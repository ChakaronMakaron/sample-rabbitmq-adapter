package com.lemakhno.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.annotation.VaultPropertySource;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.config.AbstractVaultConfiguration;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Optional;

@Configuration
@VaultPropertySource(value = "${vault.credentials}")
@VaultPropertySource(value = "${vault.configuration}")
public class VaultConfig extends AbstractVaultConfiguration {

    private static final Logger log = LoggerFactory.getLogger(VaultConfig.class);

    private static final String VAULT_HOST = "VAULT_HOST";
    private static final String VAULT_USERNAME = "VAULT_USERNAME";
    private static final String VAULT_PASS = "VAULT_PASSWORD";

    @Override
    public VaultEndpoint vaultEndpoint() {
        String vaultHost = Optional.ofNullable(System.getProperty(VAULT_HOST)).orElse(System.getenv(VAULT_HOST));
        return VaultEndpoint.from(URI.create("https://" + vaultHost));
    }

    @Override
    public ClientAuthentication clientAuthentication() {
        try {
            return new TokenAuthentication(getVaultToken());
        } catch (JSONException e) {
            log.error(e.getMessage());
        }
        return new TokenAuthentication("null");
    }

    private String getVaultToken() throws JSONException {
        String userLogin = Optional.ofNullable(System.getProperty(VAULT_USERNAME)).orElse(System.getenv(VAULT_USERNAME));
        String userPassword = Optional.ofNullable(System.getProperty(VAULT_PASS)).orElse(System.getenv(VAULT_PASS));
        String vaultHost = Optional.ofNullable(System.getProperty(VAULT_HOST)).orElse(System.getenv(VAULT_HOST));

        String authUrl = "https://" + vaultHost + "/v1/auth/userpass/login/" + userLogin;
        RestTemplate restTemplate = new RestTemplate();
        String requestBody = "{\"password\": \"" + userPassword + "\"}";
        String response = restTemplate.postForObject(authUrl, requestBody, String.class);
        return new JSONObject(response).getJSONObject("auth").getString("client_token");
    }
}
