package com.zonafranca.compressor_services.servicios;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.zonafranca.compressor_services.dto.AuditoriaDto;
import com.zonafranca.compressor_services.dto.RespuestaAuditoriaDto;

@Service
public class CompresorAuditoriaService {

    @Value("${URL_AUDITORIA_PICIZ}")
	private String urlAuditriaPiciz;

    Logger logger = LoggerFactory.getLogger(CompresorAuditoriaService.class);

    /**
     * Método que registra una auditoría en el servicio de auditoría de Piciz
     * @param auditoriaDto
     */
    public void registrarAuditoria(AuditoriaDto auditoriaDto) {
        try {
            RestTemplate restTemplate = new RestTemplate();
    
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
    
            HttpEntity<AuditoriaDto> auditoriaEntity = new HttpEntity<>(auditoriaDto, headers);
    
            ResponseEntity<RespuestaAuditoriaDto> respuesta = restTemplate.exchange(
                urlAuditriaPiciz + "/auditoria-piciz/compresor",
                HttpMethod.POST,
                auditoriaEntity,
                RespuestaAuditoriaDto.class
            );
            System.out.println("respuesta" + respuesta.getBody().getSuccess());
            if (respuesta.getBody().getSuccess()) {
                logger.info("Auditoria registrada correctamente");
            } else {
                logger.error("Error al registrar la auditoría: {}", respuesta.getBody().getMessage());
            }
        } catch (Exception e) {
            logger.error("Error al registrar la auditoría: {}", e.getMessage());
        }
    }

}
