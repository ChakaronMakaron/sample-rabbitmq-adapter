package com.lemakhno.services;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RabbitListenerManagerService {

    RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

    public void stopConsumer(String containerId) {
        try {
            if (rabbitListenerEndpointRegistry.getListenerContainer(containerId).isRunning()) {
                log.info("Stopping {}", containerId);
                rabbitListenerEndpointRegistry.getListenerContainer(containerId).stop();
            }
        } catch (NullPointerException e) {
            log.error("Cannot find container: {}", containerId);
            log.error("Exception", e);
        }
    }

    public void startConsumer(String containerId) {
        try {
            if (!rabbitListenerEndpointRegistry.getListenerContainer(containerId).isRunning()) {
                log.info("Starting {}", containerId);
                rabbitListenerEndpointRegistry.getListenerContainer(containerId).start();
            }
        } catch (NullPointerException e) {
            log.error("Cannot find container: {}", containerId);
            log.error("Exception", e);
        }
    }
}
