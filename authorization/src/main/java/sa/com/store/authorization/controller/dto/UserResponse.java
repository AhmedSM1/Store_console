
package sa.com.store.authorization.controller.dto;

import lombok.Builder;

@Builder
public record UserResponse(
    Long id,
    String username,
    String email,
    String firstname,
    String lastname,
    String phone,
    String avatar,
    String role,
    boolean enabled
) {}
