package com.lemakhno.services;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.lemakhno.config.rest.RestTemplateConfig;
import com.lemakhno.models.Item;
import com.lemakhno.properties.ConfigProperties;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemsService {

    final String RETRY_INSTANCE_NAME = "getItems";
    final ConfigProperties configProperties;
    final RestTemplate itemsServiceRestTemplate;
    
    public ItemsService(ConfigProperties configProperties, RestTemplate itemsServiceRestTemplate) {
        this.configProperties = configProperties;
        this.itemsServiceRestTemplate = itemsServiceRestTemplate;
    }

    @Retry(name = RETRY_INSTANCE_NAME)
    public ResponseEntity<List<Item>> getItems(String status) {
        String urlWithParams = UriComponentsBuilder.fromHttpUrl(configProperties.getItemsUrl())
                .queryParam("status", status)
                .build()
                .toUriString();

        return itemsServiceRestTemplate.exchange(
                urlWithParams,
                HttpMethod.GET,
                new HttpEntity<>(RestTemplateConfig.headers()),
                new ParameterizedTypeReference<>() {}
        );
    }
}
