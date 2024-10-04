package com.zonafranca.compressor_services.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")

public class RabbitConfigurattions {


    @Value("${apprabbit.queue.name}")
    private String apprabbitQueue;

    public RabbitConfigurattions() {
    }

    public RabbitConfigurattions(String apprabbitQueue) {
        this.apprabbitQueue = apprabbitQueue;
    }

    public String getApprabbitQueue() {
        return this.apprabbitQueue;
    }

    public void setApprabbitQueue(String apprabbitQueue) {
        this.apprabbitQueue = apprabbitQueue;
    }

    @Override
    public String toString() {
        return "{" +
            " apprabbitQueue='" + getApprabbitQueue() + "'}";
    }
}