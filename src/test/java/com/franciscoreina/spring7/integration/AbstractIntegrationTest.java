package com.franciscoreina.spring7.integration;

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

    @Value("${spring.security.user.name}")
    String username;

    @Value("${spring.security.user.password}")
    String password;

    @Autowired
    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = webTestClient
                .mutate()
                .defaultHeaders(header -> header.setBasicAuth(username, password))
                .build();
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
