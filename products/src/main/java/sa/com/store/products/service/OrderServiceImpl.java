package sa.com.store.products.service;

import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import sa.com.store.products.controller.dto.*;
import sa.com.store.products.entity.Order;
import sa.com.store.products.entity.ProductOrder;
import sa.com.store.products.integration.UserClient;
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


    @Override
    public CreateNewOrderResponse createNewOrder(CreateNewOrderRequest request, String token) {
        UserDTO userDetails = getUserDetails(request.username(), token);
        List<ProductOrder> productOrders = convertToProductOrders(request.products());
        DiscountDto discountDto = discountService.calculateFinalPrice(productOrders, userDetails);
        Order saved = buildAndSaveOrder(request, userDetails, productOrders, discountDto);

        return CreateNewOrderResponse.builder()
                .orderId(saved.getId())
                .priceBeforeDiscount(saved.getPriceBeforeDiscount())
                .priceAfterDiscount(saved.getPriceAfterDiscount())
                .discountType(discountDto.discountType())
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
                .status("PENDING")
                .username(userDetails.name())
                .products(productOrders)
                .priceBeforeDiscount(discountDto.priceBeforeDiscount())
                .priceAfterDiscount(discountDto.priceAfterDiscount())
                .discountType(discountDto.discountType())
                .discountAmount(discountDto.discountAmount())
                .build());
    }

    @Override
    public ConfirmOrderResponse confirmOrder(ConfirmOrderRequest request) {
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + request.orderId()));
        order.setStatus("CONFIRMED");
        order.setConfirmedDate(LocalDate.now());
        orderRepository.save(order);

        return ConfirmOrderResponse.builder()
                .orderId(request.orderId())
                .billAmount(request.billAmount())
                .build();
    }

    @Override
    public AllOrderResponse getAllOrdersByUsername(String username) {
        return null;
    }

    @Override
    public OrderDto getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        // Get current user details
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        boolean isAdmin = authorities.stream().anyMatch(a -> a.getAuthority().equals("ADMIN_DASHBOARD_ACCESS"));
        boolean isManager = authorities.stream().anyMatch(a -> a.getAuthority().equals("ADMIN_DASHBOARD_ACCESS"));
        boolean isOrderOwner = order.getUsername().equals(currentUsername);

        if (!isAdmin && !isManager && !isOrderOwner) {
            throw new IllegalArgumentException("Not authorized to view this order");
        }

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


    private UserDTO getUserDetails(String userid, String token){
        return userClient.getUserById(userid, token);
    }
}
