package sa.com.store.authorization.controller.dto;

import jakarta.validation.constraints.Email;
import lombok.Builder;

import jakarta.validation.constraints.*;

@Builder
public record UserRegistrationRequest(
        @NotBlank(message = "Username is required")
        @Pattern(regexp = "^[a-zA-Z0-9._]+$", message = "Username can only contain alphanumeric characters, dots, and underscores")
        @Size(min = 4, max = 20)
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^[+]?[0-9\\s-]+$", message = "Invalid phone number format")
        String phone,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password
) {}