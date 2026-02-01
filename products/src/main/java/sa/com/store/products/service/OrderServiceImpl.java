package sa.com.store.products.service;

import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import sa.com.store.products.aspect.Authorized;
import sa.com.store.products.controller.dto.*;
import sa.com.store.products.entity.Order;
import sa.com.store.products.entity.ProductOrder;
import sa.com.store.products.integration.UserClient;
import sa.com.store.products.mapper.OrderMapper;
import sa.com.store.products.model.UserDTO;
import sa.com.store.products.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;


@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService{

    private UserClient userClient;
    private OrderRepository orderRepository;
    private ProductService productService;
    private DiscountService discountService;
    private OrderMapper orderMapper;


    @Override
    public CreateNewOrderResponse createNewOrder(CreateNewOrderRequest request, String token) {
        UserDTO userDetails = getUserDetails(token);
        List<ProductOrder> productOrders = convertToProductOrders(request.products());
        DiscountDto discountDto = discountService.calculateFinalPrice(productOrders, userDetails);
        Order saved = buildAndSaveOrder(request, userDetails, productOrders, discountDto);

        return CreateNewOrderResponse.builder()
                .orderId(saved.getId())
                .priceBeforeDiscount(saved.getPriceBeforeDiscount())
                .priceAfterDiscount(saved.getPriceAfterDiscount())
                .discountType(discountDto.discountType())
                .discountAmount(discountDto.discountAmount())
                .build();
    }

    private List<ProductOrder> convertToProductOrders(List<ProductOrderRequest> productSelections) {
        return productSelections.stream()
                .map(selection -> {
                    ProductDTO product = productService.getProductById(selection.productId());
                    return ProductOrder.builder()
                            .productId(product.getId())
                            .category(product.getCategory())
                            .unitPrice(product.getPrice())
                            .quantity(selection.quantity())
                            .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(selection.quantity())))
                            .build();
                })
                .toList();
    }

    private Order buildAndSaveOrder(CreateNewOrderRequest request, UserDTO userDetails,
                                    List<ProductOrder> productOrders, DiscountDto discountDto) {
        return orderRepository.save(Order.builder()
                .city(request.city())
                .address(request.address())
                .createdDate(LocalDate.now())
                .status("PENDING")
                .username(userDetails.username())
                .products(productOrders)
                .priceBeforeDiscount(discountDto.priceBeforeDiscount())
                .priceAfterDiscount(discountDto.priceAfterDiscount())
                .discountType(discountDto.discountType())
                .discountAmount(discountDto.discountAmount())
                .build());
    }

    @Override
    public ConfirmOrderResponse confirmOrder(String orderId, String username ) {
        Order order = orderRepository.findByIdAndStatus(orderId, "PENDING")
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        order.setStatus("CONFIRMED");
        order.setConfirmedDate(LocalDate.now());
        orderRepository.save(order);

        return ConfirmOrderResponse.builder()
                .orderId(orderId)
                .billAmount(order.getPriceAfterDiscount())
                .build();
    }

    @Override
    public AllOrderResponse getAllOrdersByUsername(String username) {
        List<Order> orders = orderRepository.findByUsername(username);
        List<OrderDto> dtoList = orders.stream().map(orderMapper::toDTO).toList();
        return AllOrderResponse.builder()
                .orders(dtoList)
                .build();
        
        
    }

    @Override
    @Authorized
    public OrderDto getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        return OrderDto.builder()
                .id(order.getId())
                .username(order.getUsername())
                .status(order.getStatus())
                .products(order.getProducts())
                .priceBeforeDiscount(order.getPriceBeforeDiscount())
                .priceAfterDiscount(order.getPriceAfterDiscount())
                .discountType(order.getDiscountType())
                .discountAmount(order.getDiscountAmount())
                .confirmedDate(order.getConfirmedDate())
                .build();
    }



    private UserDTO getUserDetails(String token){
        return userClient.getUserById(token);
    }
}
