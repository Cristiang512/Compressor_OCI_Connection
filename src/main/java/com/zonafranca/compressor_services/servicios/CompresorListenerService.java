package com.zonafranca.compressor_services.servicios;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.zonafranca.compressor_services.utils.ConstantesAplicacion;
import java.io.ByteArrayInputStream;
import java.util.Base64;

@Service
public class CompresorListenerService {

    private static final Logger log = LoggerFactory.getLogger(CompresorListenerService.class);

    private final CompresorFtpService compresorFtpService;
    private final ObjectStorageService objectStorageService;  // Inyectamos el ObjectStorageService

    @Autowired
    public CompresorListenerService(CompresorFtpService compresorFtpService, ObjectStorageService objectStorageService) {
        this.compresorFtpService = compresorFtpService;
        this.objectStorageService = objectStorageService;
    }

    /**
     * Método que recibe los mensajes de la cola de RabbitMQ
     * @param data Mensaje recibido
     */
    @RabbitListener(queues = "compresor-archivos")
    public void recibirMensaje(final Message data) {
        try {
            String message = new String(data.getBody());
            log.info("Mensaje recibido: {}", message);

            JSONObject jsonObject = new JSONObject(message);

            if ("object-storage".equalsIgnoreCase(jsonObject.getString("tipo"))) {
                String rutaArchivo = jsonObject.getString("ruta");

                // Verificamos si el archivo existe en Object Storage
                boolean archivoExiste = objectStorageService.verificarArchivoExiste(rutaArchivo);

                if (!archivoExiste) {
                    log.error("El archivo no existe en la ruta: {}", rutaArchivo);
                } else {
                    log.info("El archivo existe en la ruta: {}", rutaArchivo);

                    // Verificar si la carpeta BackupCompressor existe, si no, crearla
                    String carpetaBackup = "BackupCompressor";
                    boolean carpetaExiste = objectStorageService.verificarCarpetaExiste(carpetaBackup);

                    if (!carpetaExiste) {
                        objectStorageService.crearCarpeta(carpetaBackup);
                        log.info("Carpeta {} creada en Object Storage", carpetaBackup);
                    }

                    // Realizar la copia del archivo en la carpeta BackupCompressor
                    objectStorageService.copiarArchivo(rutaArchivo, carpetaBackup);
                    log.info("Archivo copiado a la carpeta BackupCompressor: {}", rutaArchivo);
                }

            } else if ("file-system".equalsIgnoreCase(jsonObject.getString("tipo"))) {
                // Proceso para file-system
                String rutaArchivo = jsonObject.getString("ruta");
                compresorFtpService.procesarArchivo(jsonObject, rutaArchivo);
            } else {
                log.warn("Tipo de almacenamiento no reconocido, no se procesará el mensaje");
            }

        } catch (JSONException err) {
            log.error("Error en el parseo del mensaje JSON: {}", err.getMessage());
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                try {
                    Thread.sleep(ConstantesAplicacion.MESSAGE_RETRY_DELAY);
                } catch (InterruptedException e) {
                    log.error("Error interno en la API, no se reenviará el mensaje a la cola: {}", e.getMessage(), e);
                    throw new AmqpRejectAndDontRequeueException(e);
                }
                log.info("Lanzando excepción para que el mensaje sea reencolado.");
                throw new RuntimeException();
            } else {
                throw new AmqpRejectAndDontRequeueException(ex);
            }
        } catch (Exception e) {
            log.error("Error interno al procesar el mensaje: {}", e.getMessage(), e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }
}