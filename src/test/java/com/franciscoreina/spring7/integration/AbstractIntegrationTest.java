package com.franciscoreina.spring7.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

public abstract class AbstractIntegrationTest {

    private static final MediaType JSON = MediaType.APPLICATION_JSON;

    @Autowired
    WebTestClient webTestClient;

    protected WebTestClient.ResponseSpec postRequest(String uri, Object body) {
        return webTestClient.post()
                .uri(uri)
                .accept(JSON)
                .contentType(JSON)
                .bodyValue(body)
                .exchange();
    }

    protected WebTestClient.ResponseSpec getRequest(String uri) {
        return webTestClient.get()
                .uri(uri)
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
}
