package sa.com.store.products.model;

import java.time.LocalDate;

public record UserDTO(
        String id,
        String name,
        String role,
        LocalDate createdAt,
        boolean isAffiliate
) {
}
