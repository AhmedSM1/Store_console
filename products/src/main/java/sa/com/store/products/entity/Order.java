package sa.com.store.products.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Document(collection = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {
    String id;
    String username;
    List<ProductOrder> products;
    String address;
    String city;
    String status;
    String paymentMethod;
    String priceBeforeDiscount;
    String priceAfterDiscount;
    String discountType;
    String discountAmount;
    LocalDate createdDate;
    LocalDate confirmedDate;

}
