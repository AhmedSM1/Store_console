package sa.com.store.products.service;

import sa.com.store.products.controller.dto.*;

public interface OrderService {

    CreateNewOrderResponse createNewOrder(CreateNewOrderRequest request, String token);

    ConfirmOrderResponse confirmOrder(String orderId, String username);

    AllOrderResponse getAllOrdersByUsername(String username);

    OrderDto getOrderById(String orderId);

}
