package sa.com.store.products.model;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
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
