package com.lemakhno.properties;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.lemakhno.util.AppUtil;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConfigProperties {

    @Value("${rabbit.source.queue}")
    String rabbitSourceQueue;

    @Value("${rabbit.consumers}")
    Integer rabbitConsumers;

    @Value("${rabbit.target.exchange}")
    String rabbitTargetExchange;

    @Value("${rabbit.host}")
    String rabbitHost;

    @Value("${rabbit.maxConsumers}")
    Integer rabbitMaxConsumers;

    @Value("${rabbit.port.http}")
    Integer rabbitPortHttp;

    @Value("${rabbit.port.https}")
    Integer rabbitPortHttps;

    @Value("${rabbit.props.config.isTlsV12Enabled}")
    Boolean isRabbitTlsEnabled;

    @Value("${resilience4j.retry.instances.getItems.max-attempts}")
    Integer getItemsMaxAttempts;

    @Value("${resilience4j.retry.instances.getItems.wait-duration}")
    String getItemsWaitDuration;

    @Value("${items.service.monitoring.task.delay}")
    Long itemsServiceMonitoringTaskDelay;

    @Value("${items.service.actuator.health.url}")
    String itemsServiceActuatorHealthUrl;

    @Value("${items.service.getItems.url}")
    String itemsUrl;

    @PostConstruct
    public void postConstruct() {
        log.info("Configuration\n{}", AppUtil.prettyPrint(this));
    }
}
