
package sa.com.store.authorization.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record EmailChangeRequest(
    @NotBlank(message = "Password is required")
    String password,
    
    @NotBlank(message = "New email is required")
    @Email(message = "Invalid email format")
    String newEmail
) {}
