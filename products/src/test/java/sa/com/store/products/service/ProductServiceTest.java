package sa.com.store.products.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sa.com.store.products.controller.dto.CreateProductRequest;
import sa.com.store.products.controller.dto.ProductDTO;
import sa.com.store.products.entity.Category;
import sa.com.store.products.entity.Product;
import sa.com.store.products.mapper.ProductMapper;
import sa.com.store.products.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id("1")
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(100.0))
                .quantity(10)
                .build();

        productDTO = ProductDTO.builder()
                .id("1")
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(100.0))
                .quantity(10)
                .build();
    }

    @Test
    void createNewProduct() {
        CreateProductRequest request = new CreateProductRequest(
                "Test Product",
                "Test Description",
                BigDecimal.valueOf(100.0),
                10,
                Category.groceries
        );

        ProductDTO expectedDTO = ProductDTO.builder()
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(100.0))
                .quantity(10)
                .build();

        when(productMapper.toEntity(any(ProductDTO.class))).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toDTO(any(Product.class))).thenReturn(productDTO);

        ProductDTO savedProduct = productService.createNewProduct(request);

        assertNotNull(savedProduct);
        assertEquals("Test Product", savedProduct.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void getAllProducts() {
        when(productRepository.findAll()).thenReturn(Collections.singletonList(product));
        when(productMapper.toDTO(any(Product.class))).thenReturn(productDTO);

        List<ProductDTO> products = productService.getAllProducts();

        assertFalse(products.isEmpty());
        assertEquals(1, products.size());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void getProductById() {
        when(productRepository.findById("1")).thenReturn(Optional.of(product));
        when(productMapper.toDTO(any(Product.class))).thenReturn(productDTO);

        ProductDTO foundProduct = productService.getProductById("1");

        assertEquals("Test Product", foundProduct.getName());
        verify(productRepository, times(1)).findById("1");
    }

    @Test
    void updateProduct() {
        when(productRepository.findById("1")).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);


        ProductDTO updatedInfo = ProductDTO.builder()
                .name("Updated Name")
                .description("Updated Description")
                .price(BigDecimal.valueOf(150.0))
                .quantity(5)
                .build();
        ProductDTO updatedDTO = ProductDTO.builder()
                .id("1")
                .name("Updated Name") // Expected result
                .description("Updated Description")
                .price(BigDecimal.valueOf(150.0))
                .quantity(5)
                .build();

        when(productMapper.toDTO(any(Product.class))).thenReturn(updatedDTO);
        ProductDTO result = productService.updateProduct("1", updatedInfo);
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        verify(productRepository, times(1)).findById("1");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void deleteProduct() {
        doNothing().when(productRepository).deleteById("1");

        productService.deleteProduct("1");

        verify(productRepository, times(1)).deleteById("1");
    }
}
