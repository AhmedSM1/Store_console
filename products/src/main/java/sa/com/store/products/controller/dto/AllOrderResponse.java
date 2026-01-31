package sa.com.store.products.controller.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record AllOrderResponse(List<OrderDto> orders) {
}
