package sa.com.store.products.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sa.com.store.products.controller.dto.*;
import sa.com.store.products.service.OrderService;

@RestController
@RequestMapping("/orders")
@AllArgsConstructor
public class OrderController {
    private OrderService orderService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("#request.username == authentication.name ")
    public CreateNewOrderResponse createOrder(@RequestBody CreateNewOrderRequest request,
                                              @RequestHeader("Authorization") String token) {

       return orderService.createNewOrder(request, token);
    }

    @PostMapping(value = "/confirm")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("#request.username == authentication.name ")
    public ConfirmOrderResponse confirmOrder(ConfirmOrderRequest request) {
        return orderService.confirmOrder(request);
    }

    @GetMapping(value = "/users/{username}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("#username == authentication.name ")
    public AllOrderResponse getOrdersByUsername(@PathVariable String username) {
        return orderService.getAllOrdersByUsername(username);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public OrderDto getOrderById(@PathVariable String id) {
        return orderService.getOrderById(id);
    }


}
