package sa.com.store.products.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sa.com.store.products.controller.dto.*;
import sa.com.store.products.entity.Category;
import sa.com.store.products.entity.Order;
import sa.com.store.products.entity.ProductOrder;
import sa.com.store.products.integration.UserClient;
import sa.com.store.products.mapper.OrderMapper;
import sa.com.store.products.model.UserDTO;
import sa.com.store.products.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private UserClient userClient;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    @Mock
    private DiscountService discountService;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    @Captor
    private ArgumentCaptor<String> stringCaptor;

    private UserDTO testUserDTO;
    private ProductDTO testProductDTO;
    private Order testOrder;
    private OrderDto testOrderDto;
    private DiscountDto testDiscountDto;
    private ProductOrderRequest productOrderRequest;

    @BeforeEach
    void setUp() {
        testUserDTO = UserDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .build();

        testProductDTO = ProductDTO.builder()
                .id("product-1")
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(100.0))
                .quantity(10)
                .category(Category.electronics)
                .build();

        testDiscountDto = DiscountDto.builder()
                .priceBeforeDiscount("100.0")
                .priceAfterDiscount("90.0")
                .discountType("PERCENTAGE")
                .discountAmount("10")
                .build();

        testOrder = Order.builder()
                .id("order-1")
                .username("testuser")
                .address("123 Test Street")
                .city("Test City")
                .status("PENDING")
                .paymentMethod("CARD")
                .priceBeforeDiscount("100.0")
                .priceAfterDiscount("90.0")
                .discountType("PERCENTAGE")
                .discountAmount("10")
                .createdDate(LocalDate.now())
                .confirmedDate(null)
                .products(Collections.emptyList())
                .build();

        testOrderDto = OrderDto.builder()
                .id("order-1")
                .username("testuser")
                .address("123 Test Street")
                .city("Test City")
                .status("PENDING")
                .paymentMethod("CARD")
                .priceBeforeDiscount("100.0")
                .priceAfterDiscount("90.0")
                .discountType("PERCENTAGE")
                .discountAmount("10")
                .createdDate(LocalDate.now())
                .confirmedDate(null)
                .products(Collections.emptyList())
                .build();

        productOrderRequest = new ProductOrderRequest("product-1", 1);
    }

    @Test
    void testCreateNewOrderSuccess() {
        // Arrange
        CreateNewOrderRequest request = new CreateNewOrderRequest(
                Collections.singletonList(productOrderRequest),
                "testuser",
                "123 Test Street",
                "Test City"
        );

        ProductOrder productOrder = ProductOrder.builder()
                .productId("product-1")
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(100.0))
                .totalPrice(BigDecimal.valueOf(100.0))
                .category(Category.electronics)
                .build();

        when(userClient.getUserById("token")).thenReturn(testUserDTO);
        when(productService.getProductById("product-1")).thenReturn(testProductDTO);
        when(discountService.calculateFinalPrice(anyList(), any(UserDTO.class))).thenReturn(testDiscountDto);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        CreateNewOrderResponse response = orderService.createNewOrder(request, "token");

        // Assert
        assertNotNull(response);
        assertEquals("order-1", response.orderId());
        assertEquals("100.0", response.priceBeforeDiscount());
        assertEquals("90.0", response.priceAfterDiscount());
        assertEquals("PERCENTAGE", response.discountType());
        assertEquals("10", response.discountAmount());

        verify(userClient, times(1)).getUserById("token");
        verify(productService, times(1)).getProductById("product-1");
        verify(discountService, times(1)).calculateFinalPrice(anyList(), eq(testUserDTO));
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testCreateNewOrderSaveOrderDetails() {
        // Arrange

        CreateNewOrderRequest request = new CreateNewOrderRequest(
                Collections.singletonList(productOrderRequest),
                "testuser",
                "123 Test Street",
                "Test City"
        );

        when(userClient.getUserById("token")).thenReturn(testUserDTO);
        when(productService.getProductById("product-1")).thenReturn(testProductDTO);
        when(discountService.calculateFinalPrice(anyList(), any(UserDTO.class))).thenReturn(testDiscountDto);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        orderService.createNewOrder(request, "token");

        // Assert
        verify(orderRepository).save(orderCaptor.capture());
        Order capturedOrder = orderCaptor.getValue();
        assertEquals("testuser", capturedOrder.getUsername());
        assertEquals("123 Test Street", capturedOrder.getAddress());
        assertEquals("Test City", capturedOrder.getCity());
        assertEquals("PENDING", capturedOrder.getStatus());
        assertEquals("100.0", capturedOrder.getPriceBeforeDiscount());
        assertEquals("90.0", capturedOrder.getPriceAfterDiscount());
        assertNotNull(capturedOrder.getCreatedDate());
    }

    @Test
    void testCreateNewOrderWithMultipleProducts() {
        // Arrange
        ProductOrderRequest product2Request = new ProductOrderRequest("product-2", 2);

        CreateNewOrderRequest request = new CreateNewOrderRequest(
                List.of(productOrderRequest, product2Request),
                "testuser",
                "123 Test Street",
                "Test City"
        );

        ProductDTO product2DTO = ProductDTO.builder()
                .id("product-2")
                .name("Test Product 2")
                .price(BigDecimal.valueOf(50.0))
                .quantity(5)
                .category(Category.groceries)
                .build();

        when(userClient.getUserById("token")).thenReturn(testUserDTO);
        when(productService.getProductById("product-1")).thenReturn(testProductDTO);
        when(productService.getProductById("product-2")).thenReturn(product2DTO);
        when(discountService.calculateFinalPrice(anyList(), any(UserDTO.class))).thenReturn(testDiscountDto);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        CreateNewOrderResponse response = orderService.createNewOrder(request, "token");

        // Assert
        assertNotNull(response);
        verify(productService, times(1)).getProductById("product-1");
        verify(productService, times(1)).getProductById("product-2");
    }
    @Test
    void testConfirmOrderSuccess() {
        // Arrange
        Order pendingOrder = Order.builder()
                .id("order-1")
                .username("testuser")
                .status("PENDING")
                .priceAfterDiscount("90.0")
                .build();

        when(orderRepository.findByIdAndStatus("order-1", "PENDING")).thenReturn(Optional.of(pendingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(pendingOrder);

        // Act
        ConfirmOrderResponse response = orderService.confirmOrder("order-1", "testuser");

        // Assert
        assertNotNull(response);
        assertEquals("order-1", response.orderId());
        assertEquals("90.0", response.billAmount());

        verify(orderRepository, times(1)).findByIdAndStatus("order-1", "PENDING");
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testConfirmOrderUpdatesStatus() {
        // Arrange
        Order pendingOrder = Order.builder()
                .id("order-1")
                .username("testuser")
                .status("PENDING")
                .priceAfterDiscount("90.0")
                .build();

        when(orderRepository.findByIdAndStatus("order-1", "PENDING")).thenReturn(Optional.of(pendingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(pendingOrder);

        // Act
        orderService.confirmOrder("order-1", "testuser");

        // Assert
        verify(orderRepository).save(orderCaptor.capture());
        Order capturedOrder = orderCaptor.getValue();
        assertEquals("CONFIRMED", capturedOrder.getStatus());
        assertNotNull(capturedOrder.getConfirmedDate());
        assertEquals(LocalDate.now(), capturedOrder.getConfirmedDate());
    }

    @Test
    void testConfirmOrderNotFound() {
        // Arrange
        when(orderRepository.findByIdAndStatus("nonexistent", "PENDING")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.confirmOrder("nonexistent", "testuser")
        );
        assertEquals("Order not found: nonexistent", exception.getMessage());

        verify(orderRepository, times(1)).findByIdAndStatus("nonexistent", "PENDING");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testGetAllOrdersByUsernameSuccess() {
        // Arrange
        List<Order> orders = Collections.singletonList(testOrder);

        when(orderRepository.findByUsername("testuser")).thenReturn(orders);
        when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDto);

        // Act
        AllOrderResponse response = orderService.getAllOrdersByUsername("testuser");

        // Assert
        assertNotNull(response);
        assertFalse(response.orders().isEmpty());
        assertEquals(1, response.orders().size());
        assertEquals("order-1", response.orders().get(0).id());

        verify(orderRepository, times(1)).findByUsername("testuser");
        verify(orderMapper, times(1)).toDTO(testOrder);
    }

    @Test
    void testGetAllOrdersByUsernameMultipleOrders() {
        // Arrange
        Order order2 = Order.builder()
                .id("order-2")
                .username("testuser")
                .status("CONFIRMED")
                .build();

        List<Order> orders = List.of(testOrder, order2);
        OrderDto orderDto2 = OrderDto.builder()
                .id("order-2")
                .username("testuser")
                .status("CONFIRMED")
                .build();

        when(orderRepository.findByUsername("testuser")).thenReturn(orders);
        when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDto);
        when(orderMapper.toDTO(order2)).thenReturn(orderDto2);

        // Act
        AllOrderResponse response = orderService.getAllOrdersByUsername("testuser");

        // Assert
        assertNotNull(response);
        assertEquals(2, response.orders().size());

        verify(orderRepository, times(1)).findByUsername("testuser");
        verify(orderMapper, times(2)).toDTO(any(Order.class));
    }

    @Test
    void testGetAllOrdersByUsernameEmpty() {
        // Arrange
        when(orderRepository.findByUsername("testuser")).thenReturn(Collections.emptyList());

        // Act
        AllOrderResponse response = orderService.getAllOrdersByUsername("testuser");

        // Assert
        assertNotNull(response);
        assertTrue(response.orders().isEmpty());

        verify(orderRepository, times(1)).findByUsername("testuser");
        verify(orderMapper, never()).toDTO(any(Order.class));
    }

    @Test
    void testGetOrderByIdSuccess() {
        // Arrange
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(testOrder));

        // Act
        OrderDto result = orderService.getOrderById("order-1");

        // Assert
        assertNotNull(result);
        assertEquals("order-1", result.id());
        assertEquals("testuser", result.username());
        assertEquals("PENDING", result.status());
        assertEquals("123 Test Street", result.address());
        assertEquals("Test City", result.city());

        verify(orderRepository, times(1)).findById("order-1");
    }

    @Test
    void testGetOrderByIdVerifyAllFields() {
        // Arrange
        testOrder = Order.builder()
                .id("order-1")
                .username("testuser")
                .address("123 Test Street")
                .city("Test City")
                .status("PENDING")
                .paymentMethod("CARD")
                .priceBeforeDiscount("100.0")
                .priceAfterDiscount("90.0")
                .discountType("PERCENTAGE")
                .discountAmount("10")
                .createdDate(LocalDate.of(2025, 1, 15))
                .confirmedDate(LocalDate.of(2025, 1, 16))
                .products(Collections.emptyList())
                .build();

        when(orderRepository.findById("order-1")).thenReturn(Optional.of(testOrder));

        // Act
        OrderDto result = orderService.getOrderById("order-1");

        // Assert
        assertEquals("order-1", result.id());
        assertEquals("testuser", result.username());
        assertEquals("123 Test Street", result.address());
        assertEquals("Test City", result.city());
        assertEquals("PENDING", result.status());
        assertEquals("CARD", result.paymentMethod());
        assertEquals("100.0", result.priceBeforeDiscount());
        assertEquals("90.0", result.priceAfterDiscount());
        assertEquals("PERCENTAGE", result.discountType());
        assertEquals("10", result.discountAmount());
        assertEquals(LocalDate.of(2025, 1, 15), result.createdDate());
        assertEquals(LocalDate.of(2025, 1, 16), result.confirmedDate());
    }

    @Test
    void testGetOrderByIdNotFound() {
        // Arrange
        when(orderRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.getOrderById("nonexistent")
        );
        assertEquals("Order not found: nonexistent", exception.getMessage());

        verify(orderRepository, times(1)).findById("nonexistent");
    }

    @Test
    void testGetOrderByIdWithProducts() {
        // Arrange
        ProductOrder product1 = ProductOrder.builder()
                .productId("prod-1")
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(50.0))
                .totalPrice(BigDecimal.valueOf(100.0))
                .category(Category.electronics)
                .build();

        ProductOrder product2 = ProductOrder.builder()
                .productId("prod-2")
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(100.0))
                .totalPrice(BigDecimal.valueOf(100.0))
                .category(Category.groceries)
                .build();

        testOrder = Order.builder()
                .id("order-1")
                .username("testuser")
                .products(List.of(product1, product2))
                .build();

        when(orderRepository.findById("order-1")).thenReturn(Optional.of(testOrder));

        // Act
        OrderDto result = orderService.getOrderById("order-1");

        // Assert
        assertNotNull(result.products());
        assertEquals(2, result.products().size());
        assertEquals("prod-1", result.products().get(0).productId());
        assertEquals("prod-2", result.products().get(1).productId());

        verify(orderRepository, times(1)).findById("order-1");
    }
}
