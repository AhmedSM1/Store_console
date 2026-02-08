package sa.com.store.products.model;

import lombok.Builder;

@Builder
public record QuantityUpdateEvent(
        String  productId,
        Integer quantity
) {
}
