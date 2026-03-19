package com.franciscoreina.spring7.integration;

import com.franciscoreina.spring7.dto.auth.AccessTokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

public abstract class AbstractIntegrationTest {

    private static final MediaType JSON = MediaType.APPLICATION_JSON;

    @Autowired
    WebTestClient webTestClient;

    @Value("${test.auth-server.token-uri}")
    String tokenUri;

    @Value("${test.auth-server.client-id}")
    String clientId;

    @Value("${test.auth-server.client-secret}")
    String clientSecret;

    @Value("${test.auth-server.scope}")
    String scope;

    private String accessToken;

    @BeforeEach
    void setUp() {
        accessToken = fetchAccessToken();
        webTestClient = webTestClient
                .mutate()
                .defaultHeaders(headers -> headers.setBearerAuth(accessToken))
                .build();
    }

    private String fetchAccessToken() {
        AccessTokenResponse tokenResponse = WebTestClient.bindToServer()
                .baseUrl(tokenUri)
                .build()
                .post()
                .uri("")
                .headers(headers -> headers.setBasicAuth(clientId, clientSecret))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("grant_type=client_credentials&scope=" + scope)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AccessTokenResponse.class)
                .returnResult()
                .getResponseBody();

        if (tokenResponse == null || tokenResponse.accessToken() == null) {
            throw new IllegalStateException("Could not obtain the access token from the auth server");
        }

        return tokenResponse.accessToken();
    }

    protected WebTestClient.ResponseSpec postRequest(String uri, Object body) {
        return webTestClient.post()
                .uri(uri)
                .accept(JSON)
                .contentType(JSON)
                .bodyValue(body)
                .exchange();
    }

    protected WebTestClient.ResponseSpec getRequest(String uri) {
        return getRequest(uri, Map.of());
    }

    protected WebTestClient.ResponseSpec getRequest(String uri, Map<String, String> queryParams) {
        return webTestClient.get()
                .uri(uriBuilder(uri, queryParams))
                .accept(JSON)
                .exchange();
    }

    protected WebTestClient.ResponseSpec putRequest(String uri, Object body) {
        return webTestClient.put()
                .uri(uri)
                .accept(JSON)
                .contentType(JSON)
                .bodyValue(body)
                .exchange();
    }

    protected WebTestClient.ResponseSpec patchRequest(String uri, Object body) {
        return webTestClient.patch()
                .uri(uri)
                .accept(JSON)
                .contentType(JSON)
                .bodyValue(body)
                .exchange();
    }

    protected WebTestClient.ResponseSpec deleteRequest(String uri) {
        return webTestClient.delete()
                .uri(uri)
                .accept(JSON)
                .exchange();
    }

    private static String uriBuilder(String uri, Map<String, String> queryParams) {
        UriBuilder uriBuilder = UriComponentsBuilder.fromPath(uri);
        queryParams.forEach(uriBuilder::queryParam);
        return uriBuilder.build().toString();
    }
}
