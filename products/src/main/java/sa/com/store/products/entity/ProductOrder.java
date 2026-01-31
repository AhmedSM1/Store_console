package sa.com.store.products.entity;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProductOrder(String productId, Integer quantity, BigDecimal unitPrice, BigDecimal totalPrice, Category category) {
}
