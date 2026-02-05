package sa.com.store.products.events;

import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import sa.com.store.products.model.QuantityUpdateEvent;
import sa.com.store.products.service.ProductService;

@Component
@AllArgsConstructor
public class QuantityUpdateEventHandler {
    private ProductService productService;


    @EventListener
    @Async
    public void handleUserCreated(QuantityUpdateEvent event) {

       productService.decreaseProductQuantity(event.productId(), event.quantity());
    }


}
