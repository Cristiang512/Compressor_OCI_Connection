package com.zonafranca.compressor_services.servicios;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

	private static final String RUTA_BACKUP = "pruebabackup/";

	public void procesarArchivo(String rutaArchivo) throws IOException{
        FTPClient ftpClient = new FTPClient();
		FTPClient ftpClient1 = new FTPClient();
        try {
            // Conectar al servidor FTP
            ftpClient.connect(ftpHost, Integer.valueOf(ftpPort));
            ftpClient.login(ftpUser, ftpPassword);
			ftpClient.enterLocalPassiveMode();
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

			ftpClient1.connect(ftpHost, Integer.valueOf(ftpPort));
            ftpClient1.login(ftpUser, ftpPassword);
			ftpClient1.enterLocalPassiveMode();
			ftpClient1.setFileType(FTP.BINARY_FILE_TYPE);
			
			String rutaBase = "/picizweb/www/Digital/galleries";
			String rutaSinBase = rutaArchivo.replaceFirst(rutaBase, ""); // se realiza el replace ya que el usuario ftp ya se encuentra ubicado en /picizweb/www/Digital/galleries

			if (ftpClient.listFiles(rutaSinBase).length > 0) {
				logger.info("Copiando archivo a la carpeta de backup en el FTP...");
				String rutaBackup = rutaArchivo.replace("/picizweb/www/Digital/galleries/", RUTA_BACKUP);

				ftpCreateDirectoryTree(ftpClient, rutaBackup);
				InputStream archivoStream = ftpClient.retrieveFileStream("/" + rutaSinBase);

				boolean done = ftpClient1.storeFile(rutaBackup, archivoStream);
				if (done) {
					logger.info("Archivo copiado a la carpeta de backup en el FTP: {}", rutaBackup);
					ftpClient.logout();
					ftpClient.disconnect();
					ftpClient1.logout();
					ftpClient1.disconnect();
				} else {
					logger.info("Error al copiar el archivo a la carpeta de backup en el FTP: {} - {}", rutaBackup, ftpClient1.getReplyString());
					ftpClient.logout();
					ftpClient.disconnect();
					ftpClient1.logout();
					ftpClient1.disconnect();
				}
			} else {
				logger.warn("El archivo no existe en la ruta: {}", rutaArchivo);
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

		String[] parts = dirTree.split("/");
        String[] result = new String[parts.length - 1];
        System.arraycopy(parts, 0, result, 0, parts.length - 1);
        String newPath = String.join("/", result);
		String[] directories = newPath.split("/");

		for (String dir : directories) {
			if (!dir.isEmpty()) {
				if (dirExists) {
					dirExists = client.changeWorkingDirectory(dir);

				}
				if (!dirExists) {
					if (!client.makeDirectory(dir)) {
						throw new IOException("Unable to create remote directory '" + dir + "'.  error='"
								+ client.getReplyString() + "'");
					}

					if (!client.changeWorkingDirectory(dir)) {
						throw new IOException("Unable to change into newly created remote directory '" + dir
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
   
}
