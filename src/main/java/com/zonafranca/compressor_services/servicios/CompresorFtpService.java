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

	/**
	 * Realiza el seteo de las variables fijas para guardar la auditoria en la bd
	 * @param id
	 * @param nombreArchivo
	 * @param zonaFranca
	 * @return
	 */
	private AuditoriaDto inicializarAuditoriaDto(String id, String nombreArchivo, String zonaFranca) {
		AuditoriaDto auditoriaDto = new AuditoriaDto();
		auditoriaDto.setCodigoZf(zonaFranca);
		auditoriaDto.setIdSource(0);
		auditoriaDto.setFmmPlaca(id);
		auditoriaDto.setArchivo(nombreArchivo);
		auditoriaDto.setFecha(new Date());
		return auditoriaDto;
	}

	/**
	 * Realiza la conexion al ftp y el proceso de optimizacion del archivo
	 * @param jsonObject
	 * @param rutaArchivo
	 * @throws IOException
	 */
	public void procesarArchivo(JSONObject jsonObject, String rutaArchivo) throws IOException {
		String id = jsonObject.getString("id");
		String tipo = jsonObject.getString("tipo");
		String zonaFranca = jsonObject.getString("zonaFranca");
		String nombreArchivo = new File(rutaArchivo).getName();
	
		AuditoriaDto auditoriaDto = inicializarAuditoriaDto(id, nombreArchivo, zonaFranca);
	
		FTPClient ftpClient = new FTPClient();
		FTPClient ftpClient1 = new FTPClient();
	
		try {
			conectarAlServidorFTP(ftpClient, ftpHost, ftpPort, ftpUser, ftpPassword);
			conectarAlServidorFTP(ftpClient1, ftpHost, ftpPort, ftpUser, ftpPassword);
	
			auditoriaDto.setVchRutaFinal(rutaArchivo);
	
			String rutaBase = "/picizweb/www/Digital/galleries"; // es necesario retirar la ruta base para poder descargar el archivo, ya que la conexion al ftp se hace directo en galleries
			String rutaSinBase = rutaArchivo.replaceFirst(rutaBase, "");
	
			byte[] archivoBytes = descargarArchivoDelFTP(ftpClient, rutaSinBase);
			if (archivoBytes != null) {
				procesarArchivoYCopiarBackup(ftpClient1, auditoriaDto, rutaArchivo, nombreArchivo, rutaSinBase, archivoBytes, tipo, id);
			} else {
				logger.info("No se pudo descargar el archivo desde la ruta... tipo={}, Formulario o placa={}, ruta={}, peso antes de optimizar={}, peso despues de optimizar={}, fecha={}, duracion del proceso={}", tipo, id, rutaArchivo, 0, 0, new Date(), 0);
			}
		} catch (IOException e) {
			manejarErrorFTP(ftpClient, ftpClient1, e);
		} finally {
			desconectarDelServidorFTP(ftpClient);
			desconectarDelServidorFTP(ftpClient1);
		}
	}

	/**
	 * Realiza la conexion al servidor ftp
	 * @param ftpClient
	 * @param host
	 * @param port
	 * @param user
	 * @param password
	 * @throws IOException
	 */
	private void conectarAlServidorFTP(FTPClient ftpClient, String host, String port, String user, String password) throws IOException {
		ftpClient.connect(host, Integer.valueOf(port));
		ftpClient.login(user, password);
		ftpClient.enterLocalPassiveMode();
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
	}

	/**
	 * Se valida si el archivo existe en el ftp
	 * @param ftpClient
	 * @param rutaArchivo
	 * @return
	 * @throws IOException
	 */
	private byte[] descargarArchivoDelFTP(FTPClient ftpClient, String rutaArchivo) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		boolean success = ftpClient.retrieveFile("/" + rutaArchivo, outputStream);
		outputStream.close();
		return success ? outputStream.toByteArray() : null;
	}

	/**
	 * Se realiza la copia del archivo a la carpeta de backup
	 * @param ftpClient
	 * @param auditoriaDto
	 * @param rutaArchivo
	 * @param nombreArchivo
	 * @param rutaSinBase
	 * @param archivoBytes
	 * @param tipo
	 * @param id
	 * @throws IOException
	 */
	private void procesarArchivoYCopiarBackup(FTPClient ftpClient, AuditoriaDto auditoriaDto, String rutaArchivo, String nombreArchivo, 
	String rutaSinBase, byte[] archivoBytes, String tipo, String id) throws IOException {
		String rutaCompletaBackup = RUTA_BACKUP + nombreArchivo;
		crearDirectorioFtp(ftpClient, RUTA_BACKUP);
	
		InputStream archivoInputStream = new ByteArrayInputStream(archivoBytes);
		boolean done = ftpClient.storeFile(rutaCompletaBackup, archivoInputStream);
		archivoInputStream.close();
	
		if (done) {
			logger.info("Archivo copiado a la carpeta de backup en el FTP: {}", rutaCompletaBackup);
			manejarArchivoComprimido(ftpClient, auditoriaDto, rutaArchivo, rutaCompletaBackup, rutaSinBase, archivoBytes, tipo, id);
		} else {
			logger.info("Error al copiar el archivo a la carpeta de backup en el FTP: {} - {}", RUTA_BACKUP, ftpClient.getReplyString());
		}
	}

	/**
	 * Se realiza la compresion del archivo y se valida si se reemplaza o no
	 * @param ftpClient
	 * @param auditoriaDto
	 * @param rutaArchivo
	 * @param rutaCompletaBackup
	 * @param rutaSinBase
	 * @param archivoBytes
	 * @param tipo
	 * @param id
	 * @throws IOException
	 */
	private void manejarArchivoComprimido(FTPClient ftpClient, AuditoriaDto auditoriaDto, String rutaArchivo, String rutaCompletaBackup, 
	String rutaSinBase, byte[] archivoBytes, String tipo, String id) throws IOException {
		byte[] archivoComprimidoBytes = compresorArchivosService.comprimirArchivos(new ByteArrayInputStream(archivoBytes), rutaSinBase);
		if (archivoComprimidoBytes != null) {
			long backupSize = obtenerTamanoArchivoBackup(ftpClient, rutaCompletaBackup);
			logger.info("Tamaño del archivo de backup: {} bytes", backupSize);
			logger.info("Tamaño del archivo original: {} bytes", archivoComprimidoBytes.length);
			if (archivoComprimidoBytes.length > 0 && archivoComprimidoBytes.length < backupSize) {
				reemplazarArchivoOptimizado(ftpClient, auditoriaDto, rutaArchivo, rutaCompletaBackup, rutaSinBase, archivoComprimidoBytes, backupSize, tipo, id);
			} else {
				restaurarArchivoBackup(ftpClient, auditoriaDto, rutaArchivo, rutaCompletaBackup, backupSize, archivoComprimidoBytes, tipo, id);
			}
		} else {
			manejarErrorCompresion(ftpClient, auditoriaDto, rutaCompletaBackup, rutaArchivo);
		}
	}

	/**
	 * Se obtiene el tamaño del archivo de backup
	 * @param ftpClient
	 * @param rutaBackup
	 * @return
	 * @throws IOException
	 */
	private long obtenerTamanoArchivoBackup(FTPClient ftpClient, String rutaBackup) throws IOException {
		ftpClient.sendCommand("SIZE", rutaBackup);
		String reply = ftpClient.getReplyString();
		if (ftpClient.getReplyCode() == 213) {
			return Long.parseLong(reply.split(" ")[1].trim());
		} else {
			throw new IOException("No se pudo obtener el tamaño del archivo de backup: " + rutaBackup);
		}
	}

	/**
	 * Se reemplaza el archivo original por el archivo optimizado
	 * @param ftpClient
	 * @param auditoriaDto
	 * @param rutaArchivo
	 * @param rutaCompletaBackup
	 * @param rutaSinBase
	 * @param archivoComprimidoBytes
	 * @param backupSize
	 * @param tipo
	 * @param id
	 * @throws IOException
	 */
	private void reemplazarArchivoOptimizado(FTPClient ftpClient, AuditoriaDto auditoriaDto, String rutaArchivo, String rutaCompletaBackup, 
	String rutaSinBase, byte[] archivoComprimidoBytes, long backupSize, String tipo, String id) throws IOException {
		InputStream archivoComprimidoStream = new ByteArrayInputStream(archivoComprimidoBytes);
		ftpClient.storeFile(rutaSinBase, archivoComprimidoStream);
		archivoComprimidoStream.close();
		ftpClient.deleteFile(rutaCompletaBackup);
	
		double duracionProceso = calcularDuracionProceso(auditoriaDto);
		auditoriaDto.setVchObservacion("Archivo reemplazado por la versión optimizada");
		auditoriaDto.setFeOptimizacion(new Date());
		auditoriaDto.setNmDuracionOptimizado(duracionProceso);
		auditoriaDto.setNmTamanoArchivOrig((int) backupSize);
		auditoriaDto.setNmTamanoArchivOpti(archivoComprimidoBytes.length);
		compresorAuditoriaService.registrarAuditoria(auditoriaDto);
		logger.info("Archivo reemplazado por la versión optimizada");
		logger.info("Archivo reemplazado por la versión optimizada... tipo={}, Formulario o placa={}, ruta={}, peso antes de optimizar={}, peso despues de optimizar={}, fecha={}, duracion del proceso={}", tipo, id, rutaArchivo, backupSize, archivoComprimidoBytes.length, new Date(), duracionProceso);
	}

	/**
	 * Se restaura el archivo original por el archivo de backup
	 * @param ftpClient
	 * @param auditoriaDto
	 * @param rutaArchivo
	 * @param rutaCompletaBackup
	 * @param backupSize
	 * @param archivoComprimidoBytes
	 * @param tipo
	 * @param id
	 * @throws IOException
	 */
	private void restaurarArchivoBackup(FTPClient ftpClient, AuditoriaDto auditoriaDto, String rutaArchivo, String rutaCompletaBackup, long backupSize, byte[] archivoComprimidoBytes, String tipo, String id) throws IOException {
		ftpClient.rename(rutaCompletaBackup, rutaArchivo);
		ftpClient.deleteFile(rutaCompletaBackup);
	
		double duracionProceso = calcularDuracionProceso(auditoriaDto);
		auditoriaDto.setVchObservacion("Archivo reemplazado por el archivo de backup por no ser más pequeño");
		auditoriaDto.setFeOptimizacion(new Date());
		auditoriaDto.setNmDuracionOptimizado(duracionProceso);
		auditoriaDto.setNmTamanoArchivOrig((int) backupSize);
		auditoriaDto.setNmTamanoArchivOpti(archivoComprimidoBytes.length);
		compresorAuditoriaService.registrarAuditoria(auditoriaDto);
		logger.info("Archivo reemplazado por el archivo de backup por no ser más pequeño");
		logger.info("Archivo reemplazado por el archivo de backup... tipo={}, Formulario o placa={}, ruta={}, peso antes de optimizar={}, peso despues de optimizar={}, fecha={}, duracion del proceso={}", tipo, id, rutaArchivo, backupSize, archivoComprimidoBytes.length, new Date(), duracionProceso);
	}

	/**
	 * Se maneja el error de compresion
	 * @param ftpClient
	 * @param auditoriaDto
	 * @param rutaCompletaBackup
	 * @param rutaArchivo
	 * @throws IOException
	 */
	private void manejarErrorCompresion(FTPClient ftpClient, AuditoriaDto auditoriaDto, String rutaCompletaBackup, String rutaArchivo) throws IOException {
		ftpClient.rename(rutaCompletaBackup, rutaArchivo);
		ftpClient.deleteFile(rutaCompletaBackup);
	
		auditoriaDto.setVchObservacion("Archivo reemplazado por el archivo de backup debido a un error de optimizacion");
		auditoriaDto.setFeOptimizacion(new Date());
		compresorAuditoriaService.registrarAuditoria(auditoriaDto);
	
		logger.error("Archivo reemplazado por el archivo de backup debido a un error de optimizacion");
	}

	/**
	 * Se maneja el error de conexion al ftp
	 * @param ftpClient
	 * @param ftpClient1
	 * @param e
	 * @throws IOException
	 */
	private void manejarErrorFTP(FTPClient ftpClient, FTPClient ftpClient1, IOException e) throws IOException {
		desconectarDelServidorFTP(ftpClient);
		desconectarDelServidorFTP(ftpClient1);
		logger.error("Error al procesar el archivo en el FTP", e);
	}
	
	/**
	 * Se desconecta del servidor ftp
	 * @param ftpClient
	 * @throws IOException
	 */
	private void desconectarDelServidorFTP(FTPClient ftpClient) throws IOException {
		if (ftpClient.isConnected()) {
			ftpClient.logout();
			ftpClient.disconnect();
		}
	}
	
	/**
	 * Se calcula la duracion del proceso de optimizacion
	 * @param auditoriaDto
	 * @return
	 */
	private double calcularDuracionProceso(AuditoriaDto auditoriaDto) {
		return (double) (new Date().getTime() - auditoriaDto.getFecha().getTime()) / (60 * 1000);
	}

	/**
	 * Se crea el directorio en el ftp
	 * @param client
	 * @param dirTree
	 * @throws IOException
	 */
	private void crearDirectorioFtp(FTPClient client, String dirTree) throws IOException {

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
