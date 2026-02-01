package sa.com.store.authorization.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
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
public class UserControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;
    private WebTestClient webTestClient;

    @BeforeEach
    public void setup() {
        this.webTestClient = MockMvcWebTestClient
                .bindToApplicationContext(context)
                .apply(springSecurity())
                .build();
    }

    private static UserRegistrationRequest uniqueRegistrationRequest(String baseUsername) {
        return UserRegistrationRequest.builder()
                .username(baseUsername)
                .email(baseUsername  + "@example.com")
                .password("Password123!")
                .phone("+1234567890")
                .build();
    }

    @Test
    public void testRegisterAffiliate() {
        UserRegistrationRequest request = uniqueRegistrationRequest("affiliateuser");
        webTestClient.post()
                .uri("/users/affiliate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNotEmpty();
    }
    @Test
    public void testRegister() {
        UserRegistrationRequest request = uniqueRegistrationRequest("testuser");

        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNotEmpty();
    }

    @Test
    @WithMockUser(username = "testuser2")
    public void testGetCurrentUser() {
        UserRegistrationRequest request = uniqueRegistrationRequest("testuser2");

        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.get()
                .uri("/users/me")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.username").isEqualTo(request.username())
                .jsonPath("$.email").isEqualTo(request.email());
    }

    @Test
    @WithMockUser(username = "testuser3")
    public void testGetUserById() {
        UserRegistrationRequest request = uniqueRegistrationRequest("testuser3");

        UserRegistrationResponse registrationResponse = webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserRegistrationResponse.class)
                .returnResult()
                .getResponseBody();

        String userId = registrationResponse.id();

        webTestClient.get()
                .uri("/users/{id}", userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(userId)
                .jsonPath("$.username").isEqualTo(request.username())
                .jsonPath("$.email").isEqualTo(request.email());
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testUpdateCurrentUser() {
        UserRegistrationRequest request = uniqueRegistrationRequest("updateuser");

        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();

        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .build();

        webTestClient.put()
                .uri("/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockUser(username = "passworduser")
    public void testChangePassword() {
        UserRegistrationRequest request = uniqueRegistrationRequest("passworduser");

        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();

        PasswordChangeRequest passwordRequest = PasswordChangeRequest.builder()
                .currentPassword("OldPassword123!")
                .newPassword("NewPassword123!")
                .build();

        webTestClient.put()
                .uri("/users/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(passwordRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @WithMockUser(username = "emailuser")
    public void testChangeEmail() {
        UserRegistrationRequest request = uniqueRegistrationRequest("emailuser");

        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();

        EmailChangeRequest emailRequest = EmailChangeRequest.builder()
                .newEmail("newemail@example.com")
                .password("Password123!")
                .build();

        webTestClient.put()
                .uri("/users/me/email")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(emailRequest)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    public void testRegisterWithInvalidData() {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("")
                .email("invalid-email")
                .password("short")
                .phone("")
                .build();

        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testChangePasswordWithInvalidData() {
        PasswordChangeRequest passwordRequest = PasswordChangeRequest.builder()
                .currentPassword("wrong-old-password")
                .newPassword("123")
                .build();

        webTestClient.put()
                .uri("/users/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(passwordRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testChangeEmailWithInvalidData() {
        EmailChangeRequest emailRequest = EmailChangeRequest.builder()
                .newEmail("invalid-email")
                .build();

        webTestClient.put()
                .uri("/users/me/email")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(emailRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void testGetUserWithoutAuthentication() {
        webTestClient.get()
                .uri("/users/me")
                .exchange()
                .expectStatus().isForbidden();
    }


    @Test
    @WithMockUser(authorities = "USER_WRITE")
    public void testRegisterEmployee() {
        UserRegistrationRequest request = uniqueRegistrationRequest("employeeuser");

        webTestClient.post()
                .uri("/users/employee")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNotEmpty();
    }

    @Test
    @WithMockUser(authorities = "USER_WRITE")
    public void testRegisterAdmin() {
        UserRegistrationRequest request = uniqueRegistrationRequest("adminuser");

        webTestClient.post()
                .uri("/users/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNotEmpty();
    }

    @Test
    public void testRegisterEmployeeWithoutAuthentication() {
        UserRegistrationRequest request = uniqueRegistrationRequest("unauthorizedemployee");

        webTestClient.post()
                .uri("/users/employee")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    public void testRegisterAdminWithoutAuthentication() {
        UserRegistrationRequest request = uniqueRegistrationRequest("unauthorizedadmin");

        webTestClient.post()
                .uri("/users/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(authorities = "USER_WRITE")
    public void testRegisterEmployeeWithInvalidData() {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("")
                .email("invalid-email")
                .password("short")
                .phone("")
                .build();

        webTestClient.post()
                .uri("/users/employee")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @WithMockUser(authorities = "USER_WRITE")
    public void testRegisterAdminWithInvalidData() {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("")
                .email("invalid-email")
                .password("short")
                .phone("")
                .build();

        webTestClient.post()
                .uri("/users/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }
}