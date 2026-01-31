package sa.com.store.products.controller.dto;

import lombok.Builder;

@Builder
public record CreateNewOrderResponse(
        String orderId,
        String priceBeforeDiscount,
        String priceAfterDiscount,
        String discountType,
        String discountAmount
) {
}
