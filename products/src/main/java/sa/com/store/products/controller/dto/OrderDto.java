package sa.com.store.products.controller.dto;

import lombok.Builder;
import sa.com.store.products.entity.ProductOrder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record OrderDto(
        String id,
        String username,
        List<ProductOrder>products,
        String address,
        String city,
        String status,
        String paymentMethod,
        String priceBeforeDiscount,
        String priceAfterDiscount,
        String discountType,
        String discountAmount,
        LocalDate createdDate,
        LocalDate confirmedDate
) {
}
