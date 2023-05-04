package com.lemakhno.config.rest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LoggingInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        log.info("Request: {} {}", request.getMethodValue(), request.getURI());
        ClientHttpResponse response = new ClientHttpResponseWrapper(execution.execute(request, body));
        log.info("Response: {} {}\n{}",
                response.getRawStatusCode(),
                response.getStatusText(),
                StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8));

        return response;
    }
}
