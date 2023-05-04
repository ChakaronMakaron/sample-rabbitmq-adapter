package com.lemakhno.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CredProperties {

    @Value("${rabbit.username}")
    String rabbitUsername;

    @Value("${rabbit.password}")
    String rabbitPassword;

    @Value("${items.service.username}")
    String itemsServiceUsername;

    @Value("${items.service.password}")
    String itemsServicePassword;
}
