package com.franciscoreina.spring7.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

public abstract class AbstractJwtMockIntegrationTest extends AbstractIntegrationTest {

    private static final String FAKE_TOKEN = "fake-token";
    private static final String TEST_USER = "test-user";

    @MockitoBean
    protected JwtDecoder jwtDecoder;

    @BeforeEach
    void setUpJwtMock() {
        // Mock JWT
        Jwt jwt = Jwt.withTokenValue(FAKE_TOKEN)
                .header("alg", "none")
                .claim("sub", TEST_USER)
                .claim("scope", scope)
                .build();

        given(jwtDecoder.decode(anyString())).willReturn(jwt);

        // Override WebTestClient to always use fake token
        webTestClient = webTestClient
                .mutate()
                .defaultHeaders(headers -> headers.setBearerAuth(FAKE_TOKEN))
                .build();
    }

    @Override
    @BeforeEach
    void setUp() {
        // Intentionally overridden to skip real token retrieval from auth server
    }
}
