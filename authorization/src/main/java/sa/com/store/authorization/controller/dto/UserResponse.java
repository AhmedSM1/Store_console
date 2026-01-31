
package sa.com.store.authorization.controller.dto;

import lombok.Builder;

@Builder
public record UserResponse(
    Long id,
    String username,
    String email,
    String phone,
    String role,
    boolean enabled
) {}
