package sa.com.store.products.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import sa.com.store.products.entity.Order;


public interface OrderRepository extends MongoRepository<Order, String> {
}
