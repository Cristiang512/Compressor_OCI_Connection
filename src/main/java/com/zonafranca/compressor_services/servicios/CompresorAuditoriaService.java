package com.zonafranca.compressor_services.servicios;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zonafranca.compressor_services.dao.AuditoriaDao;
import com.zonafranca.compressor_services.dto.AuditoriaDto;

@Service
public class CompresorAuditoriaService {

    @Autowired
    private AuditoriaDao auditoriaDao;

    public void registrarAuditoria(AuditoriaDto auditoriaDto) {
        auditoriaDao.insertarRegistroAuditoria(auditoriaDto);
    }

    public void actualizarAuditoria(AuditoriaDto auditoriaDto) {
        auditoriaDao.actualizarRegistroAuditoria(auditoriaDto);
    }
}
