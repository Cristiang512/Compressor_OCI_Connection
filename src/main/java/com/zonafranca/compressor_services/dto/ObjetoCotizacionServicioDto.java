package com.zonafranca.compressor_services.dto;

public class ObjetoCotizacionServicioDto {
    private String zfCode;
    private String companyCode;
    private String shipmentNumber;
    private ObjetoSenderDto sender;
    private ObjetoCargoDetailsDto cargoDetails;

    public String getZfCode() {
        return zfCode;
    }
    public void setZfCode(String zfCode) {
        this.zfCode = zfCode;
    }
    public String getCompanyCode() {
        return companyCode;
    }
    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }
    public String getShipmentNumber() {
        return shipmentNumber;
    }
    public void setShipmentNumber(String shipmentNumber) {
        this.shipmentNumber = shipmentNumber;
    }
    public ObjetoSenderDto getSender() {
        return sender;
    }
    public void setSender(ObjetoSenderDto sender) {
        this.sender = sender;
    }
    public ObjetoCargoDetailsDto getCargoDetails() {
        return cargoDetails;
    }
    public void setCargoDetails(ObjetoCargoDetailsDto cargoDetails) {
        this.cargoDetails = cargoDetails;
    }

}
