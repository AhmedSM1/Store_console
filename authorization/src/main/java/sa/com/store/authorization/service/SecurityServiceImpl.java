package sa.com.store.authorization.service;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sa.com.store.authorization.config.JWTTokenProvider;
import sa.com.store.authorization.controller.dto.*;
import sa.com.store.authorization.data.RefreshToken;
import sa.com.store.authorization.repository.RefreshTokenRepository;
import sa.com.store.authorization.data.UserEntity;
import sa.com.store.authorization.exception.AuthorizationException;

import java.time.Instant;

@Service
@AllArgsConstructor
@Transactional
public class SecurityServiceImpl implements SecurityService {

    private final AuthenticationManager authenticationManager;
    private final JWTTokenProvider tokenProvider;
    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SecurityUserDetailsService userDetailsService;




    @Override
    public UserLoginResponse login(UserLoginRequest request) {
        try {
            // Authenticate with Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );
            String accessToken = tokenProvider.generateJwtToken(authentication);
            // Get user information
            UserEntity user =userService.getUserEntityByUsername(request.username());
            RefreshToken refreshToken = createRefreshToken(user);
            return UserLoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .build();
        } catch (AuthenticationException e) {
            throw new AuthorizationException("Invalid username or password", HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public TokenRefreshResponse refreshAccessToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.refreshToken();

        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new AuthorizationException("Refresh token not found", HttpStatus.UNAUTHORIZED));
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new AuthorizationException("Refresh token expired. Please login again", HttpStatus.UNAUTHORIZED);
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(
                refreshToken.getUser().getUsername()
        );
        String newAccessToken = tokenProvider.generateJwtTokenFromUserDetails(userDetails);
        String newRefreshToken = refreshToken.getToken();
        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }


    /**
     * Creates a new refresh token for a user
     */
    private RefreshToken createRefreshToken(UserEntity user) {
        var refreshToken = tokenProvider.createRefreshToken(user);
        // Save and return
        return refreshTokenRepository.save(refreshToken);
    }
}
