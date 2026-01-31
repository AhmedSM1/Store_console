package sa.com.store.authorization.service;

import sa.com.store.authorization.controller.dto.*;

public interface SecurityService {
    UserLoginResponse login(UserLoginRequest request);

    TokenRefreshResponse refreshAccessToken(TokenRefreshRequest request);


}
