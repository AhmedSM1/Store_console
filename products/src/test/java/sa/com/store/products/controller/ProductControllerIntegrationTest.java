package sa.com.store.products.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import sa.com.store.products.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "ENV=test"
        }
)
class ProductControllerIntegrationTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ProductRepository productRepository;

    @BeforeEach
    void cleanDb() {
        productRepository.deleteAll();
    }

    @Test
    void getEndpoints_arePublic_writeEndpoints_requireManagerRole() {
        // GET all (public)
        webTestClient.get()
                .uri("/api/products")
                .exchange()
                .expectStatus().isOk();

        // Create (requires MANAGER)
        Map<String, Object> createRequest = Map.of(
                "name", "Keyboard",
                "description", "Mechanical",
                "price", new BigDecimal("250.00"),
                "quantity", 5
        );

        Map created = webTestClient
                .mutateWith(mockJwt().authorities(() -> "ROLE_MANAGER"))
                .post()
                .uri("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        assertThat(created).isNotNull();
        String id = String.valueOf(created.get("id"));
        assertThat(id).isNotBlank();

        // GET by id (public)
        webTestClient.get()
                .uri("/api/products/{id}", id)
                .exchange()
                .expectStatus().isOk();

        // Search (public)
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/products/search").queryParam("name", "key").build())
                .exchange()
                .expectStatus().isOk();

        // Update (requires MANAGER)
        Map<String, Object> updateRequest = Map.of(
                "name", "Keyboard Pro",
                "description", "Mechanical",
                "price", new BigDecimal("300.00"),
                "quantity", 4
        );

        webTestClient
                .mutateWith(mockJwt().authorities(() -> "ROLE_MANAGER"))
                .put()
                .uri("/api/products/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk();

        // Delete (requires MANAGER)
        webTestClient
                .mutateWith(mockJwt().authorities(() -> "ROLE_MANAGER"))
                .delete()
                .uri("/api/products/{id}", id)
                .exchange()
                .expectStatus().isNoContent();

        // After delete => 404 (public)
        webTestClient.get()
                .uri("/api/products/{id}", id)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void writeEndpoints_withoutToken_areUnauthorized() {
        Map<String, Object> createRequest = Map.of(
                "name", "Mouse",
                "description", "Wireless",
                "price", new BigDecimal("120.00"),
                "quantity", 10
        );

        webTestClient.post()
                .uri("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void writeEndpoints_withWrongRole_areForbidden() {
        Map<String, Object> createRequest = Map.of(
                "name", "Monitor",
                "description", "27 inch",
                "price", new BigDecimal("900.00"),
                "quantity", 2
        );

        webTestClient
                .mutateWith(mockJwt().authorities(() -> "ROLE_USER"))
                .post()
                .uri("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isForbidden();
    }
}
