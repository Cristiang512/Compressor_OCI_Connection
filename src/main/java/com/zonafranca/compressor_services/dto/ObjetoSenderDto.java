package com.zonafranca.compressor_services.dto;

public class ObjetoSenderDto {
    private String name;
    private String city;
    private Integer municipalityCode;
    private String address;
    private Integer documentType;
    private String documentNumber;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public Integer getMunicipalityCode() {
        return municipalityCode;
    }
    public void setMunicipalityCode(Integer municipalityCode) {
        this.municipalityCode = municipalityCode;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public Integer getDocumentType() {
        return documentType;
    }
    public void setDocumentType(Integer documentType) {
        this.documentType = documentType;
    }
    public String getDocumentNumber() {
        return documentNumber;
    }
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

}
