package sa.com.store.products.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;
import sa.com.store.products.entity.Product;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByNameContainingIgnoreCase(String name);
    @Query("{ 'category': ?0 }")
    List<Product> findByCategory(String category);



    @Query("{ '_id': ?0 }")
    @Update("{ '$inc': { 'quantity': -?1 } }")
    void  decreaseProductQuantity(String  productId, Integer quantity);


}
