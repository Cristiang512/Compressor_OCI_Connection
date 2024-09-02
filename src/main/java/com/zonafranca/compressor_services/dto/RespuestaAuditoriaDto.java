package com.zonafranca.compressor_services.dto;


public class RespuestaAuditoriaDto {

    private boolean success;
    private String message;

    public boolean getSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

}
