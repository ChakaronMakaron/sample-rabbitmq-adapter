package com.lemakhno.config;

import com.lemakhno.properties.ConfigProperties;
import com.lemakhno.properties.CredProperties;
import com.rabbitmq.client.ConnectionFactory;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

@Configuration
public class RabbitConfig {

    public static final String LISTENER_NAME = "sourceQueueListener";

    private final ConfigProperties configProperties;
    private final CredProperties credProperties;

    public RabbitConfig(ConfigProperties configProperties, CredProperties credProperties) {
        this.configProperties = configProperties;
        this.credProperties = credProperties;
    }

    @Bean
    public CachingConnectionFactory connectionFactory() throws NoSuchAlgorithmException, KeyManagementException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setAutomaticRecoveryEnabled(false);
        connectionFactory.setHost(configProperties.getRabbitHost());
        connectionFactory.setUsername(credProperties.getRabbitUsername());
        connectionFactory.setPassword(credProperties.getRabbitPassword());
        if (configProperties.getIsRabbitTlsEnabled()) {
            connectionFactory.useSslProtocol("TLSv1.2");
            connectionFactory.setPort(configProperties.getRabbitPortHttps());
        } else {
            connectionFactory.setPort(configProperties.getRabbitPortHttp());
        }
        return new CachingConnectionFactory(connectionFactory);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(CachingConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrentConsumers(configProperties.getRabbitConsumers());
        factory.setMaxConcurrentConsumers(configProperties.getRabbitMaxConsumers());
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    @Bean
    public RabbitTemplate targetExchangePublishRabbitTemplate(CachingConnectionFactory connectionFactory, ConfigProperties configProperties) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setExchange(configProperties.getRabbitTargetExchange());
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }
}
