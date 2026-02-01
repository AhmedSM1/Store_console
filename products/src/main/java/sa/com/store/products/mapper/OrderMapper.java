package sa.com.store.products.mapper;

import org.springframework.stereotype.Component;
import sa.com.store.products.controller.dto.OrderDto;
import sa.com.store.products.entity.Order;

@Component
public class OrderMapper {

    public OrderDto toDTO(Order order) {
        if (order == null) {
            return null;
        }
        return OrderDto.builder()
                .id(order.getId())
                .username(order.getUsername())
                .status(order.getStatus())
                .products(order.getProducts())
                .priceBeforeDiscount(order.getPriceBeforeDiscount())
                .priceAfterDiscount(order.getPriceAfterDiscount())
                .discountType(order.getDiscountType())
                .discountAmount(order.getDiscountAmount())
                .confirmedDate(order.getConfirmedDate())
                .build();
    }
}
