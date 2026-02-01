package sa.com.store.products.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import sa.com.store.products.entity.Order;

import java.util.List;
import java.util.Optional;


public interface OrderRepository extends MongoRepository<Order, String> {

    @Query("{ 'id': ?0 , 'status': ?1 }")
    Optional<Order> findByIdAndStatus(String orderId, String status);

    @Query("{ 'username': ?0 }")
    List<Order> findByUsername(String username);

}
