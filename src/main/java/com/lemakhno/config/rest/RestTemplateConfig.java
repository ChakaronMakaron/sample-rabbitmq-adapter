
package com.lemakhno.config.rest;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.lemakhno.properties.CredProperties;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;

@Configuration
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RestTemplateConfig {

    public static HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        return headers;
    }

    @Bean
    public RestTemplate itemsServiceRestTemplate(CredProperties credProperties, LoggingInterceptor loggingInterceptor)
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        httpClientBuilder.setSSLSocketFactory(noSslCheckSocketFactory());

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClientBuilder.build());

        return new RestTemplateBuilder()
                .basicAuthentication(credProperties.getItemsServiceUsername(), credProperties.getItemsServicePassword())
                .interceptors(loggingInterceptor)
                .requestFactory(() -> requestFactory)
                .build();
    }

    private SSLConnectionSocketFactory noSslCheckSocketFactory() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial((chain, authType) -> true).build();
        return new SSLConnectionSocketFactory(sslContext);
    }
}
