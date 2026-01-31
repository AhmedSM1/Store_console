package sa.com.store.products.controller.dto;


import java.util.List;

public record CreateNewOrderRequest (
        List<ProductOrderRequest> products,
        String username,
        String address,
        String city
){

}
