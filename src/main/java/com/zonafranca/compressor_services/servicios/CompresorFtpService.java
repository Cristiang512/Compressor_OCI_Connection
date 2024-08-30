package com.zonafranca.compressor_services.servicios;

import java.util.Date;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.zonafranca.compressor_services.dto.AuditoriaDto;

@Service
public class CompresorFtpService {

    @Value("${ftp.host}")
	private String ftpHost;
	@Value("${ftp.port}")
	private String ftpPort;
	@Value("${ftp.user}")
	private String ftpUser;
	@Value("${ftp.password}")
	private String ftpPassword;

    Logger logger = LoggerFactory.getLogger("compresorFtpService");

	private static final String RUTA_BACKUP = "/pruebabackup/";
	
	private final CompresorArchivosService compresorArchivosService;
	private final CompresorAuditoriaService compresorAuditoriaService;

	@Autowired
	public CompresorFtpService(CompresorArchivosService compresorArchivosService, CompresorAuditoriaService compresorAuditoriaService) {
		this.compresorArchivosService = compresorArchivosService;
		this.compresorAuditoriaService = compresorAuditoriaService;
	}

	public void procesarArchivo(JSONObject jsonObject, String rutaArchivo) throws IOException {
		String id = jsonObject.getString("id");
		String tipo = jsonObject.getString("tipo");
		String nombreArchivo = new File(rutaArchivo).getName();

		AuditoriaDto auditoriaDto = new AuditoriaDto();
		auditoriaDto.setFmmPlaca(id);
		auditoriaDto.setArchivo(nombreArchivo);
		auditoriaDto.setFecha(new Date());
		auditoriaDto.setVchOpe("Optimizacion archivos file-system");


		FTPClient ftpClient = new FTPClient();
		FTPClient ftpClient1 = new FTPClient();
		try {
			Date fechaInicioProceso = new Date();
			// Conectar al servidor FTP
			ftpClient.connect(ftpHost, Integer.valueOf(ftpPort));
			ftpClient.login(ftpUser, ftpPassword);
			ftpClient.enterLocalPassiveMode();
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
	
			ftpClient1.connect(ftpHost, Integer.valueOf(ftpPort));
			ftpClient1.login(ftpUser, ftpPassword);
			ftpClient1.enterLocalPassiveMode();
			ftpClient1.setFileType(FTP.BINARY_FILE_TYPE);
			auditoriaDto.setVchRutaFinal(rutaArchivo);
	
			String rutaBase = "/picizweb/www/Digital/galleries";
			String rutaSinBase = rutaArchivo.replaceFirst(rutaBase, "");
	
			// Descargar el archivo original a un ByteArrayOutputStream
			ByteArrayOutputStream originalFileStream = new ByteArrayOutputStream();
			boolean success = ftpClient.retrieveFile("/" + rutaSinBase, originalFileStream);
			originalFileStream.close();
			
			if (success) {
				byte[] archivoBytes = originalFileStream.toByteArray();
				
				// Copiar el archivo a la carpeta de backup
				String rutaCompletaBackup = RUTA_BACKUP + nombreArchivo;
				ftpCreateDirectoryTree(ftpClient, RUTA_BACKUP);
				InputStream archivoInputStream = new ByteArrayInputStream(archivoBytes);
				boolean done = ftpClient1.storeFile(rutaCompletaBackup, archivoInputStream);
				archivoInputStream.close();
				
				if (done) {
					logger.info("Archivo copiado a la carpeta de backup en el FTP: {}", rutaCompletaBackup);
					
					byte[] archivoComprimidoBytes = compresorArchivosService.comprimirArchivos(new ByteArrayInputStream(archivoBytes), rutaSinBase);
					if (archivoComprimidoBytes != null) {
						ftpClient1.sendCommand("SIZE", rutaCompletaBackup);
						String reply = ftpClient1.getReplyString();
						
						if (ftpClient1.getReplyCode() == 213) { // Código de respuesta para el tamaño del archivo
							long backupSize = Long.parseLong(reply.split(" ")[1].trim());
							logger.info("Tamaño del archivo de backup: {} bytes", backupSize);
							logger.info("Tamaño del archivo original: {} bytes", archivoComprimidoBytes.length);
							
							if (archivoComprimidoBytes.length > 0 && archivoComprimidoBytes.length < backupSize) {
								InputStream archivoComprimidoStream = new ByteArrayInputStream(archivoComprimidoBytes);
								ftpClient1.storeFile(rutaSinBase, archivoComprimidoStream);
								archivoComprimidoStream.close();
								ftpClient1.deleteFile(rutaCompletaBackup);

								double duracionProceso = new Date().getTime() - (double)fechaInicioProceso.getTime() / 1000;
								auditoriaDto.setVchObservacion("Archivo reemplazado por la versión optimizada");
								auditoriaDto.setFeOptimizacion(new Date());
								auditoriaDto.setNmDuracionOptimizado(duracionProceso);
								auditoriaDto.setNmTamanoArchivOrig((int) backupSize);
								auditoriaDto.setNmTamanoArchivOpti(archivoComprimidoBytes.length);
								compresorAuditoriaService.registrarAuditoria(auditoriaDto);
								logger.info("Archivo reemplazado por la versión optimizada... tipo={}, Formulario o placa={}, ruta={}, peso antes de optimizar={}, peso despues de optimizar={}, fecha={}, duracion del proceso={}", tipo, id, rutaArchivo, backupSize, archivoComprimidoBytes.length, new Date(), duracionProceso);
							} else {
								ftpClient1.rename(rutaCompletaBackup, rutaArchivo);
								ftpClient1.deleteFile(rutaCompletaBackup);

								double duracionProceso = new Date().getTime() - ((double) fechaInicioProceso.getTime() / 1000);
								auditoriaDto.setVchObservacion("Archivo reemplazado por el archivo de backup por no ser más pequeño");
								auditoriaDto.setFeOptimizacion(new Date());
								auditoriaDto.setNmDuracionOptimizado(duracionProceso);
								auditoriaDto.setNmTamanoArchivOrig((int) backupSize);
								auditoriaDto.setNmTamanoArchivOpti(archivoComprimidoBytes.length);
								compresorAuditoriaService.registrarAuditoria(auditoriaDto);
								logger.info("Archivo de backup eliminado: {}", rutaCompletaBackup);
								logger.info("Archivo reemplazado por el archivo de backup... tipo={}, Formulario o placa={}, ruta={}, peso antes de optimizar={}, peso despues de optimizar={}, fecha={}, duracion del proceso={}", tipo, id, rutaArchivo, backupSize, archivoComprimidoBytes.length, new Date(), duracionProceso);
							}
						} else {
							ftpClient1.rename(rutaCompletaBackup, rutaArchivo);
							ftpClient1.deleteFile(rutaCompletaBackup);

							auditoriaDto.setVchObservacion("Archivo reemplazado por el archivo de backup se presento un error");
							auditoriaDto.setFeOptimizacion(new Date());
							compresorAuditoriaService.registrarAuditoria(auditoriaDto);
							logger.info("Archivo de backup eliminado: {}", rutaCompletaBackup);
							logger.info("Archivo reemplazado por el archivo de backup");
							logger.error("No se pudo obtener el tamaño del archivo de backup: {}", rutaCompletaBackup);
						}
					} else {
						auditoriaDto.setVchObservacion("Archivo reemplazado por el archivo de backup se presento un error");
						auditoriaDto.setFeOptimizacion(new Date());
						compresorAuditoriaService.registrarAuditoria(auditoriaDto);
						double duracionProceso = new Date().getTime() - ((double) fechaInicioProceso.getTime() / 1000);
						logger.error("Error al optimizar el archivo... tipo={}, Formulario o placa={}, ruta={}, peso antes de optimizar={}, peso despues de optimizar={}, fecha={}, duracion del proceso={}", tipo, id, rutaArchivo, null, null, new Date(), duracionProceso);
					}
					
					
					ftpClient.logout();
					ftpClient.disconnect();
					ftpClient1.logout();
					ftpClient1.disconnect();
				} else {
					logger.info("Error al copiar el archivo a la carpeta de backup en el FTP: {} - {}", RUTA_BACKUP, ftpClient1.getReplyString());
					ftpClient.logout();
					ftpClient.disconnect();
					ftpClient1.logout();
					ftpClient1.disconnect();
				}
			} else {
				logger.warn("No se pudo descargar el archivo desde la ruta: {}", rutaArchivo);
			}
		} catch (IOException e) {
			ftpClient.logout();
			ftpClient.disconnect();
			ftpClient1.logout();
			ftpClient1.disconnect();
			logger.error("Error al procesar el archivo en el FTP", e);
		}
	}

	private void ftpCreateDirectoryTree(FTPClient client, String dirTree) throws IOException {

		boolean dirExists = true;

		if (!dirTree.isEmpty()) {
			if (dirExists) {
				dirExists = client.changeWorkingDirectory(dirTree);

			}
			if (!dirExists) {
				if (!client.makeDirectory(dirTree)) {
					throw new IOException("No se puede crear el directorio '" + dirTree + "'.  error='"
							+ client.getReplyString() + "'");
				}

				if (!client.changeWorkingDirectory(dirTree)) {
					throw new IOException("No es posible acceder al nuevo directorio '" + dirTree
							+ "'.  error='" + client.getReplyString() + "'");
				}

				logger.info("Respuesta FTP server: {}", client.getReplyCode());
				logger.info("Respuesta FTP server: {}", client.getReplyString());

				logger.info("Asignando permisos al directorio creado");
				client.sendCommand("SITE CHMOD 777 .");
				logger.info("Asigando permisos al directorio: {}", client.getReplyCode());
				logger.info("Asignando permisos al directorio: {}", client.getReplyString());

			}
		}
	}

}
