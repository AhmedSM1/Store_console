package sa.com.store.products.controller.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import sa.com.store.products.entity.Category;

import java.math.BigDecimal;

public record CreateProductRequest(

        @NotBlank(message = "name is required")
        @Size(min = 2, max = 100, message = "name must be between 2 and 100 characters")
        String name,

        @Size(max = 1000, message = "description must be at most 1000 characters")
        String description,

        @NotNull(message = "price is required")
        @Positive(message = "price must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "price must have up to 10 integer digits and up to 2 decimal places")
        BigDecimal price,

        @NotNull(message = "quantity is required")
        @Min(value = 0, message = "quantity must be 0 or greater")
        Integer quantity,
        @NotNull(message = "category is required")
        Category category
) {
}
