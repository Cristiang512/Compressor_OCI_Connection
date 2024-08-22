package com.zonafranca.compressor_services.dto;

public class ObjetoCargoDetailsDto {
    private String packagingType;
    private String packageQuantity;
    private String weight;
    private String cargoValue;

    public String getPackagingType() {
        return packagingType;
    }
    public void setPackagingType(String packagingType) {
        this.packagingType = packagingType;
    }
    public String getPackageQuantity() {
        return packageQuantity;
    }
    public void setPackageQuantity(String packageQuantity) {
        this.packageQuantity = packageQuantity;
    }
    public String getWeight() {
        return weight;
    }
    public void setWeight(String weight) {
        this.weight = weight;
    }
    public String getCargoValue() {
        return cargoValue;
    }
    public void setCargoValue(String cargoValue) {
        this.cargoValue = cargoValue;
    }

}
