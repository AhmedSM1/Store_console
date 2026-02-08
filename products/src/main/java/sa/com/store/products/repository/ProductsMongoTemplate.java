package sa.com.store.products.repository;

import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import sa.com.store.products.entity.Product;

@Component
@AllArgsConstructor
public class ProductsMongoTemplate {
    private final MongoTemplate mongoTemplate;

    public void decreaseProductQuantity(String productId, Integer orderedQuantity) {
        Query query = new Query(Criteria.where("_id").is(productId));
        Update update = new Update().inc("quantity", -orderedQuantity);
        mongoTemplate.updateFirst(query, update, Product.class);
    }
}
