package sa.com.store.authorization.controller.dto;

import lombok.Builder;

@Builder
public record UserLoginResponse(String accessToken, String refreshToken) {
}
