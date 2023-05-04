package com.lemakhno.config.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.util.Objects.isNull;

public class ClientHttpResponseWrapper implements ClientHttpResponse {

    private ClientHttpResponse clientHttpResponse;
    private byte[] body;

    public ClientHttpResponseWrapper(ClientHttpResponse clientHttpResponse) {
        this.clientHttpResponse = clientHttpResponse;
    }

    @Override
    public HttpStatus getStatusCode() throws IOException {
        return clientHttpResponse.getStatusCode();
    }

    @Override
    public int getRawStatusCode() throws IOException {
        return clientHttpResponse.getRawStatusCode();
    }

    @Override
    public String getStatusText() throws IOException {
        return clientHttpResponse.getStatusText();
    }

    @Override
    public void close() {
        clientHttpResponse.close();
    }

    @Override
    public InputStream getBody() throws IOException {
        if (isNull(this.body)) {
            this.body = StreamUtils.copyToByteArray(clientHttpResponse.getBody());
        }
        return new ByteArrayInputStream(this.body);
    }

    @Override
    public HttpHeaders getHeaders() {
        return clientHttpResponse.getHeaders();
    }
}
