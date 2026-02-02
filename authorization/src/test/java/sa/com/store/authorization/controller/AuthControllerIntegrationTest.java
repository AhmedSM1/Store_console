package sa.com.store.authorization.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.context.WebApplicationContext;

import sa.com.store.authorization.controller.dto.*;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "ENV=test"
        }
)
class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private WebTestClient webTestClient;


    @BeforeEach
    void setup() {
        this.webTestClient = MockMvcWebTestClient
                .bindToApplicationContext(context)
                .apply(springSecurity())
                .build();
    }


    private static UserRegistrationRequest registrationRequest(String username) {
        return UserRegistrationRequest.builder()
                .username(username)
                .email(username + "@example.com")
                .password("Password123!")
                .phone("+1234567890")
                .build();
    }

    private void createUser(String username) {
        UserRegistrationRequest request = registrationRequest(username);

        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
     void testLoginSuccess() {
        String username = "loginuser";
        createUser(username);

        webTestClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserLoginRequest(username, "Password123!"))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.accessToken").isNotEmpty()
                .jsonPath("$.refreshToken").isNotEmpty();
    }

    @Test
     void testLoginWithInvalidCredentials() {
        webTestClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserLoginRequest("does-not-exist", "wrong-password"))
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
     void testRefreshTokenSuccess() {
        String username = "refreshuser";
        createUser(username);

        UserLoginResponse loginResponse = webTestClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserLoginRequest(username, "Password123!"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserLoginResponse.class)
                .returnResult()
                .getResponseBody();

        webTestClient.post()
                .uri("/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(TokenRefreshRequest.builder()
                        .refreshToken(loginResponse.refreshToken())
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.accessToken").isNotEmpty()
                .jsonPath("$.refreshToken").isNotEmpty();
    }

    @Test
     void testRefreshTokenWithInvalidToken() {
        webTestClient.post()
                .uri("/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(TokenRefreshRequest.builder()
                        .refreshToken("invalid-refresh-token")
                        .build())
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
     void testLoginWithInvalidPayload() {
        webTestClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UserLoginRequest("", ""))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
     void testRefreshWithInvalidPayload() {
        webTestClient.post()
                .uri("/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(TokenRefreshRequest.builder()
                        .refreshToken("")
                        .build())
                .exchange()
                .expectStatus().isBadRequest();
    }
}
