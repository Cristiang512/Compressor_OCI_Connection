package com.zonafranca.compressor_services;

import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

import com.zonafranca.compressor_services.config.RabbitConfigurattions;

@SpringBootApplication
@ComponentScan(basePackages = {"com.zonafranca.configuration", "com.zonafranca.compressor_services"})
public class CompressorServicesApplication extends SpringBootServletInitializer implements RabbitListenerConfigurer {

	public static void main(String[] args) {
		SpringApplication.run(CompressorServicesApplication.class, args);
	}

	@PostConstruct
	void started() {
		TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
	}

	@Autowired
    private RabbitConfigurattions rabbitConfigurattions;
    public RabbitConfigurattions getRabbitConfigurattions() {
        return rabbitConfigurattions;
    }

    public void setRabbitConfigurattions(RabbitConfigurattions rabbitConfigurattions) {
        this.rabbitConfigurattions = rabbitConfigurattions;
	}
	
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(CompressorServicesApplication.class);
    }

    /* This bean is to read the properties file configs */
    @Bean
    public RabbitConfigurattions applicationConfig() {
        return new RabbitConfigurattions();
    }

    /* Creating a bean for the Message queue */
    @Bean
    public org.springframework.amqp.core.Queue getApprabbitQueue() {
        return new org.springframework.amqp.core.Queue(getRabbitConfigurattions().getApprabbitQueue());
    }

    /* Bean for rabbitTemplate */
    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public MappingJackson2MessageConverter consumerJackson2MessageConverter() {
        return new MappingJackson2MessageConverter();
    }

    @Bean
    public DefaultMessageHandlerMethodFactory messageHandlerMethodFactory() {
        DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
        factory.setMessageConverter(consumerJackson2MessageConverter());
        return factory;
    }

    @Override
    public void configureRabbitListeners(final RabbitListenerEndpointRegistrar registrar) {
        registrar.setMessageHandlerMethodFactory(messageHandlerMethodFactory());
    }

}