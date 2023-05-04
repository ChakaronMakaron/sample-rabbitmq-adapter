package com.lemakhno.services;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.lemakhno.config.rest.RestTemplateConfig;
import com.lemakhno.properties.ConfigProperties;

import static com.lemakhno.config.RabbitConfig.LISTENER_NAME;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemsServiceAvailabilityMonitor {

    final ConfigProperties configProperties;
    final RabbitListenerManagerService listenerManagerService;
    final RestTemplate itemsServiceRestTemplate;
    final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    ScheduledFuture<?> monitoringTask = null;
    boolean isActive = false;

    public ItemsServiceAvailabilityMonitor(ConfigProperties configProperties, RabbitListenerManagerService listenerManagerService, RestTemplate itemsServiceRestTemplate) {
        this.configProperties = configProperties;
        this.listenerManagerService = listenerManagerService;
        this.itemsServiceRestTemplate = itemsServiceRestTemplate;
    }

    public synchronized void startMonitor() {
        if (this.isActive) {
            log.info("Items service availability monitor is already active");
            return;
        }

        log.info("Items service availability monitor started");
        this.isActive = true;
        listenerManagerService.stopConsumer(LISTENER_NAME);

        Runnable runnable = () -> {
            log.info("Checking items service availability");
            try {
                ResponseEntity<?> response =
                        itemsServiceRestTemplate.exchange(
                                configProperties.getItemsServiceActuatorHealthUrl(),
                                HttpMethod.GET,
                                new HttpEntity<>(RestTemplateConfig.headers()),
                                new ParameterizedTypeReference<>() {}
                        );

                if (response.getStatusCodeValue() == 200) {
                    listenerManagerService.startConsumer(LISTENER_NAME);
                    this.isActive = false;
                    this.monitoringTask.cancel(true);
                }
            } catch (RestClientResponseException e) {
                log.error("Error while getting items service status: {} {}", e.getRawStatusCode(), e.getStatusText());
            }
        };

        this.monitoringTask = executorService.scheduleAtFixedRate(
                runnable,
                configProperties.getItemsServiceMonitoringTaskDelay(),
                configProperties.getItemsServiceMonitoringTaskDelay(),
                TimeUnit.MILLISECONDS
        );
    }
}
