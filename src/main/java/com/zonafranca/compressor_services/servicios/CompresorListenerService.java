package com.zonafranca.compressor_services.servicios;

import javax.servlet.http.HttpServletRequest;

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

@Service
public class CompresorListenerService {

    private static final Logger log = LoggerFactory.getLogger(CompresorListenerService.class);

	private final CompresorFtpService compresorFtpService;
	
	@Autowired
	public CompresorListenerService(CompresorFtpService compresorFtpService) {
		this.compresorFtpService = compresorFtpService;
	}

    @RabbitListener(queues = "compresor-archivos")
    public void recibirMensaje(final Message data) {
       try {
			String message = new String(data.getBody());
			log.info("Mensaje recibido: {}", message);
			try {
				JSONObject jsonObject = new JSONObject(message);

				if ("file-system".equalsIgnoreCase(jsonObject.getString("tipo"))) {
					String rutaArchivo = jsonObject.getString("ruta");
					compresorFtpService.procesarArchivo(jsonObject, rutaArchivo);
				} else {
					log.warn("El tipo no es 'file-system', no se procesará el mensaje");
				}
			}catch (JSONException err){
				System.out.println("Error: " + err.getMessage());
			}
		} catch (HttpClientErrorException ex) {
			if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
				try {
					Thread.sleep(ConstantesAplicacion.MESSAGE_RETRY_DELAY);
				} catch (InterruptedException e) {
					log.error("Internal server error occurred in API call. Bypassing message requeue {}", e.getMessage(), e);
					throw new AmqpRejectAndDontRequeueException(e);
				}
				log.info("Throwing exception so that message will be requed in the queue.");
				// Note: Typically Application specific exception should be thrown below
				throw new RuntimeException();
			} else {
				throw new AmqpRejectAndDontRequeueException(ex);
			}
		} catch (Exception e) {
			log.error("Internal server error occurred in API call. Bypassing message requeue {}", e.getMessage(), e);
			throw new AmqpRejectAndDontRequeueException(e);
		}

    }
}