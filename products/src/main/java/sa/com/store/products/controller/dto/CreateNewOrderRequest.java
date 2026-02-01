package sa.com.store.products.controller.dto;


import lombok.NonNull;

import java.util.List;

public record CreateNewOrderRequest (
        @NonNull List<ProductOrderRequest> products,
        @NonNull String username,
        @NonNull String address,
        @NonNull String city
){

}
