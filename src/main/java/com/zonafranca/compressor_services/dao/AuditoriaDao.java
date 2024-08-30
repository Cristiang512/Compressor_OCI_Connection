package com.zonafranca.compressor_services.dao;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zonafranca.compressor_services.dto.AuditoriaDto;
import com.zonafranca.compressor_services.entidades.Auditoria;

@Repository
public interface AuditoriaDao extends JpaRepository<Auditoria, Integer>{

    @Query(value = "SELECT ad FROM Auditoria ad WHERE ad.fmmPlaca = :fmmPlaca AND archivo = :archivo")
    List<Auditoria> obtenerRegistroAuditoria(@Param("fmmPlaca") String fmmPlaca, @Param("archivo") String archivo);

    @Modifying
    @Query(value = "INSERT INTO TZFW_ARCHIVOS_DIG (FMM_PLACA, ARCHIVO, FECHA, CDUSUARIO, ID_SOURCE," +
                    "VCHMODULO, VCHOPE, VCHRUTA_FINAL, VCHOBSERVACION, FEOPTIMIZACION, NMDURACION_OPTIMIZADO, NMTAMANO_ARCHIV_ORIG, NMTAMANO_ARCHIV_OPTI) VALUES " +
                    "(:#{#registro.fmmPlaca}, :#{#registro.archivo}, :#{#registro.fecha}, :#{#registro.cdUsuario}," +
                    ":#{#registro.idSource}, :#{#registro.vchModulo}, :#{#registro.vchOpe}, :#{#registro.vchRutaFinal}," +
                    ":#{#registro.vchObservacion}, :#{#registro.feOptimizacion}, :#{#registro.nmDuracionOptimizado}," +
                    ":#{#registro.nmTamanoArchivOrig}, :#{#registro.nmTamanoArchivOpti})", nativeQuery = true)
    @Transactional
    void insertarRegistroAuditoria(@Param("registro") AuditoriaDto registro);
    
    @Modifying
    @Query(value = "UPDATE TZFW_ARCHIVOS_DIG SET FMM_PLACA = :#{#dto.fmmPlaca}, " +
                "ARCHIVO = :#{#dto.archivo}, FECHA = :#{#dto.fecha}, " +
                "CDUSUARIO = :#{#dto.cdUsuario}, ID_SOURCE = :#{#dto.idSource}, " +
                "VCHMODULO = :#{#dto.vchModulo}, VCHOPE = :#{#dto.vchOpe}, " +
                "VCHRUTA_FINAL = :#{#dto.vchRutaFinal}, VCHOBSERVACION = :#{#dto.vchObservacion}, " +
                "FEOPTIMIZACION = :#{#dto.feOptimizacion}, " +
                "NMDURACION_OPTIMIZADO = :#{#dto.nmDuracionOptimizado}, " +
                "NMTAMANO_ARCHIV_ORIG = :#{#dto.nmTamanoArchivOrig}, " +
                "NMTAMANO_ARCHIV_OPTI = :#{#dto.nmTamanoArchivOpti} " +
                "WHERE FMM_PLACA = :#{#dto.fmmPlaca} AND ARCHIVO = :#{#dto.archivo}",
        nativeQuery = true)
    @Transactional
    void actualizarRegistroAuditoria(@Param("dto") AuditoriaDto dto);

}
