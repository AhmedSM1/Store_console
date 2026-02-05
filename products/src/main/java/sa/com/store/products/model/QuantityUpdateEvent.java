package sa.com.store.products.model;

import lombok.Builder;
import lombok.Data;

@Builder
public record QuantityUpdateEvent(
        String  productId,
        Integer quantity
) {
}
