package sa.com.store.products.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sa.com.store.products.controller.dto.CreateProductRequest;
import sa.com.store.products.controller.dto.ProductDTO;
import sa.com.store.products.entity.Category;
import sa.com.store.products.service.ProductService;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;
    private ProductDTO testProductDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(productController)
                .build();

        testProductDTO = ProductDTO.builder()
                .id("1")
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(100.0))
                .quantity(10)
                .build();
    }

    @Test
    @WithMockUser(authorities = "PRODUCT_WRITE")
    void testCreateProduct() throws Exception {
        ProductDTO createdProduct = ProductDTO.builder()
                .id("1")
                .name("New Product")
                .description("New Description")
                .price(BigDecimal.valueOf(150.0))
                .quantity(5)
                .category(Category.electronics)
                .build();

        when(productService.createNewProduct(any(CreateProductRequest.class)))
                .thenReturn(createdProduct);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"New Product\",\"description\":\"New Description\",\"price\":150.0,\"quantity\":5,\"category\":\"electronics\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("New Product"));
    }

    @Test
    void testGetAllProducts() throws Exception {
        when(productService.getAllProducts())
                .thenReturn(Collections.singletonList(testProductDTO));

        mockMvc.perform(get("/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetProductById() throws Exception {
        when(productService.getProductById("1"))
                .thenReturn(testProductDTO);

        mockMvc.perform(get("/products/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testSearchProducts() throws Exception {
        when(productService.searchProducts("Test"))
                .thenReturn(Collections.singletonList(testProductDTO));

        mockMvc.perform(get("/products/search")
                        .param("name", "Test")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "PRODUCT_DELETE")
    void testDeleteProduct() throws Exception {
        doNothing().when(productService).deleteProduct("1");

        mockMvc.perform(delete("/products/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }


    @Test
    void testGetProductsByCategory() throws Exception {
        when(productService.getAllProductsByCategory("Electronics"))
                .thenReturn(Collections.singletonList(testProductDTO));

        mockMvc.perform(get("/products/categories/Electronics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "PRODUCT_READ")
    void testUpdateProduct() throws Exception {

        when(productService.updateProduct(any(String.class), any(ProductDTO.class)))
                .thenReturn( ProductDTO.builder()
                        .id("1")
                        .name("Updated Product")
                        .description("Updated Description")
                        .price(BigDecimal.valueOf(200.0))
                        .quantity(15)
                        .build());

        mockMvc.perform(put("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Product\",\"description\":\"Updated Description\",\"price\":200.0,\"quantity\":15}"))
                .andExpect(status().isOk());
    }
}