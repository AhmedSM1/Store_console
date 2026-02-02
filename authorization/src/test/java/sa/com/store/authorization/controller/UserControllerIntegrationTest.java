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
 class UserControllerIntegrationTest {

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

    private UserRegistrationResponse createUser(String username) {
        UserRegistrationRequest request = uniqueRegistrationRequest(username);
        return webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserRegistrationResponse.class)
                .returnResult()
                .getResponseBody();

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
     void testRegisterAffiliate() {
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
     void testRegister() {
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
     void testGetCurrentUser() {
        createUser("testuser2");
        webTestClient.get()
                .uri("/users/me")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.username").isEqualTo("testuser2")
                .jsonPath("$.email").isNotEmpty();
    }

    @Test
    @WithMockUser(username = "testuser3")
     void testGetUserByUsername() {
        createUser("testuser3");
        webTestClient.get()
                .uri("/users/{username}", "testuser3")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.username").isEqualTo("testuser3")
                .jsonPath("$.email").isNotEmpty();
    }

    @Test
    @WithMockUser(username = "testuser")
     void testUpdateCurrentUser() {
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
     void testChangePassword() {
        this.createUser("passworduser");
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
     void testChangeEmail() {
        this.createUser("emailuser");
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

    @ParameterizedTest
    @CsvSource({" , Password123!, admin@gmail.com, 05555555555", " testuser, short, admin@gmail.com,05555555555", " testuser, Password123!, invalid-email,05555555555",
            " testuser, Password123!, admin@gmail.com ,wrongPhoneNum"})
     void testRegisterWithInvalidData(String username,  String password,String email, String phone) {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username(username)
                .email(email)
                .password(password)
                .phone(phone)
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
     void testChangePasswordWithInvalidData() {
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
     void testChangeEmailWithInvalidData() {
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
     void testGetUserWithoutAuthentication() {
        webTestClient.get()
                .uri("/users/me")
                .exchange()
                .expectStatus().isForbidden();
    }


    @ParameterizedTest
    @CsvSource({"admin,adminUser", "employee,employeeuser"})
    @WithMockUser(authorities = "USER_WRITE")
    void testRegisterEmployee_success(String role, String username) {
        UserRegistrationRequest request = uniqueRegistrationRequest(username);

        webTestClient.post()
                .uri("/users/"+ role)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isNotEmpty();
    }


    @ParameterizedTest
    @CsvSource({"admin", "employee"})
     void testRegisterWithoutAuthentication(String role) {
        UserRegistrationRequest request = uniqueRegistrationRequest("unauthorizedemployee");

        webTestClient.post()
                .uri("/users/"+ role)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isForbidden();
    }


    @ParameterizedTest
    @CsvSource({" , Password123!, admin@gmail.com, 05555555555", " testuser, short, admin@gmail.com,05555555555", " testuser, Password123!, invalid-email,05555555555",
            " testuser, Password123!, admin@gmail.com ,wrongPhoneNum"})
    @WithMockUser(authorities = "USER_WRITE")
     void testRegisterEmployeeWithInvalidData(String username,  String password,String email, String phone) {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username(username)
                .email(email)
                .password(password)
                .phone(phone)
                .build();

        webTestClient.post()
                .uri("/users/employee")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @ParameterizedTest
    @CsvSource({" , Password123!, admin@gmail.com, 05555555555", " testuser, short, admin@gmail.com,05555555555", " testuser, Password123!, invalid-email,05555555555",
            " testuser, Password123!, admin@gmail.com ,wrongPhoneNum"})

    @WithMockUser(authorities = "USER_WRITE")
     void testRegisterAdminWithInvalidData(String username,  String password,String email, String phone) {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username(username)
                .email(email)
                .password(password)
                .phone(phone)
                .build();
        webTestClient.post()
                .uri("/users/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }
}