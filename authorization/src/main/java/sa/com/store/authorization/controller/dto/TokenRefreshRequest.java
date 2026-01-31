package sa.com.store.authorization.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record TokenRefreshRequest(
        @NotBlank(message = "refreshToken is required") String refreshToken) {
}
