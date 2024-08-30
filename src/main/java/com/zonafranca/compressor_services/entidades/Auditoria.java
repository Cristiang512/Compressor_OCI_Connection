package com.zonafranca.compressor_services.entidades;

import java.util.Date;
 
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
 
@Entity
@Table(name = "TZFW_ARCHIVOS_DIG")
public class Auditoria {
    @Id
    @Column(name = "ID")
    private Integer id;
 
    @Column(name = "FMM_PLACA")
    private String fmmPlaca;
 
    @Column(name = "ARCHIVO")
    private String archivo;
    
    @Column(name = "FECHA")
    private Date fecha;
 
    @Column(name = "CDUSUARIO")
    private String cdUsuario;

    @Column(name = "ID_SOURCE")
    private Integer idSource;

    @Column(name = "VCHMODULO")
    private String vchModulo;

    @Column(name = "VCHOPE")
    private String vchOpe;

    @Column(name = "VCHRUTA_FINAL")
    private String vchRutaFinal;

    @Column(name = "VCHOBSERVACION")
    private String vchObservacion;

    @Column(name = "FEOPTIMIZACION")
    private Date feOptimizacion;

    @Column(name = "NMDURACION_OPTIMIZADO")
    private Double nmDuracionOptimizado;

    @Column(name = "NMTAMANO_ARCHIV_ORIG")
    private Integer nmTamanoArchivOrig;

    @Column(name = "NMTAMANO_ARCHIV_OPTI")
    private Integer nmTamanoArchivOpti;

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
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
 
 
    
 
}