package sa.com.store.products.service;

import org.springframework.stereotype.Service;
import sa.com.store.products.controller.dto.DiscountDto;
import sa.com.store.products.entity.Category;
import sa.com.store.products.entity.ProductOrder;
import sa.com.store.products.model.UserDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class DiscountService {
    private static final BigDecimal EMPLOYEE_DISCOUNT_PERCENTAGE = new BigDecimal("0.30");
    private static final BigDecimal AFFILIATE_DISCOUNT_PERCENTAGE = new BigDecimal("0.10");
    private static final BigDecimal LOYALTY_DISCOUNT_PERCENTAGE = new BigDecimal("0.05");
    private static final BigDecimal FLAT_DISCOUNT_PER_HUNDRED = new BigDecimal("5");
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final int LONG_TERM_CUSTOMER_YEARS = 2;

    public DiscountDto calculateFinalPrice(List<ProductOrder> items, UserDTO user) {
        BigDecimal totalAmount = calculateItemsTotal(items);
        BigDecimal nonGroceryAmount = calculateNonGroceryTotal(items);

        BigDecimal percentage = getUserDiscountPercentage(user);
        BigDecimal percentageDiscountAmount = nonGroceryAmount.multiply(percentage);
        BigDecimal amountAfterPercentage = totalAmount.subtract(percentageDiscountAmount);
        BigDecimal flatDiscount = amountAfterPercentage.divide(HUNDRED, 0, RoundingMode.FLOOR)
                .multiply(FLAT_DISCOUNT_PER_HUNDRED);

        BigDecimal finalAmount = amountAfterPercentage.subtract(flatDiscount).max(BigDecimal.ZERO);

        String discountType = determineDiscountType(user);
        BigDecimal totalDiscountAmount = percentageDiscountAmount.add(flatDiscount);

        return DiscountDto.builder()
                .priceBeforeDiscount(totalAmount.toString())
                .priceAfterDiscount(finalAmount.toString())
                .discountType(discountType)
                .discountAmount(totalDiscountAmount.toString())
                .build();
    }

    private BigDecimal calculateItemsTotal(List<ProductOrder> items) {
        return items.stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateNonGroceryTotal(List<ProductOrder> items) {
        return items.stream()
                .filter(item -> !item.category().equals(Category.groceries))
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String determineDiscountType(UserDTO user) {
        if (user.role().contains("ROLE_MANAGER")) {
            return "Employee Discount (30%)";
        } else if (user.isAffiliate()) {
            return "Affiliate Discount (10%)";
        } else if (isLongTermCustomer(user.creationTime())) {
            return "Loyalty Discount (5%)";
        }
        return "Regular discount";
    }

    private BigDecimal getUserDiscountPercentage(UserDTO user) {
        if (user.role().contains("ROLE_MANAGER")) {
            return EMPLOYEE_DISCOUNT_PERCENTAGE;
        } else if (user.isAffiliate()) {
            return AFFILIATE_DISCOUNT_PERCENTAGE;
        } else if (isLongTermCustomer(user.creationTime())) {
            return LOYALTY_DISCOUNT_PERCENTAGE;
        }
        return BigDecimal.ZERO;
    }

    private boolean isLongTermCustomer(OffsetDateTime regDate) {
        return regDate != null && ChronoUnit.YEARS.between(regDate, OffsetDateTime.now()) >= LONG_TERM_CUSTOMER_YEARS;
    }
}