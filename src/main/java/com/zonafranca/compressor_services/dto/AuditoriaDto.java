package com.zonafranca.compressor_services.dto;

import java.util.Date;

public class AuditoriaDto {
    private String fmmPlaca;
    private String archivo;
    private Date fecha;
    private String cdUsuario;
    private Integer idSource;
    private String vchModulo;
    private String vchOpe;
    private String vchRutaFinal;
    private String vchObservacion;
    private Date feOptimizacion;
    private Double nmDuracionOptimizado;
    private Integer nmTamanoArchivOrig;
    private Integer nmTamanoArchivOpti;
    private String codigoZf;

    public String getFmmPlaca() {
        return fmmPlaca;
    }
    public void setFmmPlaca(String fmmPlaca) {
        this.fmmPlaca = fmmPlaca;
    }
    public String getArchivo() {
        return archivo;
    }
    public void setArchivo(String archivo) {
        this.archivo = archivo;
    }
    public Date getFecha() {
        return fecha;
    }
    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
    public String getCdUsuario() {
        return cdUsuario;
    }
    public void setCdUsuario(String cdUsuario) {
        this.cdUsuario = cdUsuario;
    }
    public Integer getIdSource() {
        return idSource;
    }
    public void setIdSource(Integer idSource) {
        this.idSource = idSource;
    }
    public String getVchModulo() {
        return vchModulo;
    }
    public void setVchModulo(String vchModulo) {
        this.vchModulo = vchModulo;
    }
    public String getVchOpe() {
        return vchOpe;
    }
    public void setVchOpe(String vchOpe) {
        this.vchOpe = vchOpe;
    }
    public String getVchRutaFinal() {
        return vchRutaFinal;
    }
    public void setVchRutaFinal(String vchRutaFinal) {
        this.vchRutaFinal = vchRutaFinal;
    }
    public String getVchObservacion() {
        return vchObservacion;
    }
    public void setVchObservacion(String vchObservacion) {
        this.vchObservacion = vchObservacion;
    }
    public Date getFeOptimizacion() {
        return feOptimizacion;
    }
    public void setFeOptimizacion(Date feOptimizacion) {
        this.feOptimizacion = feOptimizacion;
    }
    public Double getNmDuracionOptimizado() {
        return nmDuracionOptimizado;
    }
    public void setNmDuracionOptimizado(Double nmDuracionOptimizado) {
        this.nmDuracionOptimizado = nmDuracionOptimizado;
    }
    public Integer getNmTamanoArchivOrig() {
        return nmTamanoArchivOrig;
    }
    public void setNmTamanoArchivOrig(Integer nmTamanoArchivOrig) {
        this.nmTamanoArchivOrig = nmTamanoArchivOrig;
    }
    public Integer getNmTamanoArchivOpti() {
        return nmTamanoArchivOpti;
    }
    public void setNmTamanoArchivOpti(Integer nmTamanoArchivOpti) {
        this.nmTamanoArchivOpti = nmTamanoArchivOpti;
    }
    public String getCodigoZf() {
        return codigoZf;
    }
    public void setCodigoZf(String codigoZf) {
        this.codigoZf = codigoZf;
    }
    
}
