package com.franciscoreina.spring7.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

public abstract class AbstractJwtMockIntegrationTest extends AbstractIntegrationTest {

    @MockitoBean
    JwtDecoder jwtDecoder;

    @BeforeEach
    void setUpJwtMock() {
        // Mock JWT
        Jwt jwt = Jwt.withTokenValue("fake-token")
                .header("alg", "none")
                .claim("sub", "test-user")
                .claim("scope", "message.read message.write")
                .build();

        given(jwtDecoder.decode(anyString())).willReturn(jwt);

        // Override WebTestClient to always use fake token
        webTestClient = webTestClient
                .mutate()
                .defaultHeaders(headers -> headers.setBearerAuth("fake-token"))
                .build();
    }

    @Override
    @BeforeEach
    void setUp() {
        // Avoid fetchAccessToken()
    }
}
