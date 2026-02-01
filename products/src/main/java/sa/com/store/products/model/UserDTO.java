package sa.com.store.products.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record UserDTO(
        Long id,
        String username,
        String email,
        String phone,
        String role,
        boolean enabled,
        boolean isAffiliate,
        OffsetDateTime creationTime

) {
}
