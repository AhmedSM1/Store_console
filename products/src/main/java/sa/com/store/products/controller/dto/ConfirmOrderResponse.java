package sa.com.store.products.controller.dto;

import lombok.Builder;

@Builder
public record ConfirmOrderResponse(String orderId, String billAmount) {
}
