package sa.com.store.authorization.controller.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UserUpdateRequest(
    @Size(max = 50)
    String firstname,
    
    @Size(max = 50)
    String lastname,
    
    @Pattern(regexp = "^[+]?[0-9\\s-]+$", message = "Invalid phone number format")
    String phone,
    
    String avatar
) {}
