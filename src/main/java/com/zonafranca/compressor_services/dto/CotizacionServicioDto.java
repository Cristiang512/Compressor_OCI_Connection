package com.zonafranca.compressor_services.dto;

import java.util.List;

public class CotizacionServicioDto {
    List<ObjetoCotizacionServicioDto> lista;
    String urlWsOferta;
    String correoUsuario;
    String codigoZf;
    String idCompania;

    public List<ObjetoCotizacionServicioDto> getLista() {
        return this.lista;
    }
    
    public void setLista(List<ObjetoCotizacionServicioDto> lista) {
        this.lista = lista;
    }

    public String getUrlWsOferta() {
        return urlWsOferta;
    }

    public void setUrlWsOferta(String urlWsOferta) {
        this.urlWsOferta = urlWsOferta;
    }

    public String getCorreoUsuario() {
        return correoUsuario;
    }

    public void setCorreoUsuario(String correoUsuario) {
        this.correoUsuario = correoUsuario;
    }

    public String getCodigoZf() {
        return codigoZf;
    }

    public void setCodigoZf(String codigoZf) {
        this.codigoZf = codigoZf;
    }

    public String getIdCompania() {
        return idCompania;
    }

    public void setIdCompania(String idCompania) {
        this.idCompania = idCompania;
    }

}
