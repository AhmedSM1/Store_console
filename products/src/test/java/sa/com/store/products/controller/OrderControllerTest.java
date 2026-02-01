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
import sa.com.store.products.controller.dto.ConfirmOrderRequest;
import sa.com.store.products.controller.dto.ConfirmOrderResponse;
import sa.com.store.products.controller.dto.CreateNewOrderRequest;
import sa.com.store.products.controller.dto.CreateNewOrderResponse;
import sa.com.store.products.controller.dto.AllOrderResponse;
import sa.com.store.products.controller.dto.OrderDto;
import sa.com.store.products.service.OrderService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;
    private CreateNewOrderResponse createNewOrderResponse;
    private ConfirmOrderResponse confirmOrderResponse;
    private AllOrderResponse allOrderResponse;
    private OrderDto orderDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(orderController)
                .build();

        createNewOrderResponse = CreateNewOrderResponse.builder()
                .orderId("order-1")
                .priceBeforeDiscount("100.0")
                .priceAfterDiscount("90.0")
                .discountType("PERCENTAGE")
                .discountAmount("10")
                .build();

        confirmOrderResponse = ConfirmOrderResponse.builder()
                .orderId("order-1")
                .billAmount("90.0")
                .build();

        orderDto = OrderDto.builder()
                .id("order-1")
                .username("testuser")
                .address("123 Test Street")
                .city("Test City")
                .status("PENDING")
                .paymentMethod("CARD")
                .build();

        allOrderResponse = AllOrderResponse.builder()
                .orders(Collections.singletonList(orderDto))
                .build();
    }

    @Test
    @WithMockUser(username = "testuser")
    void testCreateOrder() throws Exception {
        when(orderService.createNewOrder(any(CreateNewOrderRequest.class), anyString()))
                .thenReturn(createNewOrderResponse);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content("{\"username\":\"testuser\",\"products\":[],\"address\":\"123 Test Street\",\"city\":\"Test City\",\"paymentMethod\":\"CARD\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testConfirmOrder() throws Exception {
        when(orderService.confirmOrder(anyString(), anyString()))
                .thenReturn(confirmOrderResponse);

        mockMvc.perform(post("/orders/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"order-1\",\"username\":\"testuser\",\"discountType\":\"PERCENTAGE\",\"discountAmount\":\"10\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testGetOrdersByUsername() throws Exception {
        when(orderService.getAllOrdersByUsername("testuser"))
                .thenReturn(allOrderResponse);

        mockMvc.perform(get("/orders/users/testuser")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testGetOrderById() throws Exception {
        when(orderService.getOrderById("order-1"))
                .thenReturn(orderDto);

        mockMvc.perform(get("/orders/order-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
