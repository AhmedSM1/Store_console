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
    @PreAuthorize("authentication.name == #request.username")

    public CreateNewOrderResponse createOrder(@RequestBody CreateNewOrderRequest request,
                                              @RequestHeader("Authorization") String token) {

       return orderService.createNewOrder(request, token);
    }

    @PutMapping(value = "/confirm")
    @ResponseStatus(HttpStatus.CREATED)

    @PreAuthorize("authentication.name == #username")
    public ConfirmOrderResponse confirmOrder(@RequestParam String orderId, @RequestParam String username) {
        return orderService.confirmOrder(orderId,username);
    }

    @GetMapping(value = "/users/{username}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('PRODUCT_READ') || authentication.name == #username")
    public AllOrderResponse getOrdersByUsername(@PathVariable String username) {
        return orderService.getAllOrdersByUsername(username);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public OrderDto getOrderById(@PathVariable String id) {
        return orderService.getOrderById(id);
    }


}
