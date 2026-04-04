package com.franciscoreina.spring7.integration;

import com.franciscoreina.spring7.dto.auth.AccessTokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.MySQLContainer;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@AutoConfigureWebTestClient
public abstract class AbstractIntegrationTest {

    private static final MediaType JSON = MediaType.APPLICATION_JSON;
    private static final MediaType FORM_URLENCODED = MediaType.APPLICATION_FORM_URLENCODED;

    @ServiceConnection
    static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.4");

    static {
        // Start once for the whole JVM to avoid container shutdown between IT classes
        mysqlContainer.start();
    }

    @Autowired
    protected WebTestClient webTestClient;

    @Value("${test.auth-server.token-uri}")
    protected String tokenUri;

    @Value("${test.auth-server.client-id}")
    protected String clientId;

    @Value("${test.auth-server.client-secret}")
    protected String clientSecret;

    @Value("${test.auth-server.scope}")
    protected String scope;

    @BeforeEach
    void setUp() {
        var accessToken = fetchAccessToken();
        webTestClient = webTestClient.mutate()
                .defaultHeaders(headers -> headers.setBearerAuth(accessToken))
                .build();
    }

    private String fetchAccessToken() {
        var tokenResponse = authClient().post()
                .uri("")
                .headers(headers -> headers.setBasicAuth(clientId, clientSecret))
                .contentType(FORM_URLENCODED)
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

    private WebTestClient authClient() {
        return WebTestClient.bindToServer()
                .baseUrl(tokenUri)
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
        var uriBuilder = UriComponentsBuilder.fromPath(uri);
        queryParams.forEach(uriBuilder::queryParam);
        return uriBuilder.build().toString();
    }
}