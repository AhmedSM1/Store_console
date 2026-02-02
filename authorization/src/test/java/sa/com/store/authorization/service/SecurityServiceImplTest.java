package sa.com.store.authorization.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import sa.com.store.authorization.config.JWTTokenProvider;
import sa.com.store.authorization.controller.dto.UserLoginRequest;
import sa.com.store.authorization.controller.dto.UserLoginResponse;
import sa.com.store.authorization.data.RefreshToken;
import sa.com.store.authorization.data.UserEntity;
import sa.com.store.authorization.repository.RefreshTokenRepository;
import sa.com.store.authorization.exception.AuthorizationException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityServiceImplTest {

    @InjectMocks
    private SecurityServiceImpl securityService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTTokenProvider jwtTokenProvider;

    @Mock
    private UserService userService;  // ADD THIS

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private SecurityUserDetailsService userDetailsService;  // MISSING

    @Test
    void login_validCredentials_returnsAccessTokenAndRefreshToken() {
        // Arrange
        UserLoginRequest request = new UserLoginRequest("validUsername", "validPassword");
        Authentication authentication = Mockito.mock(Authentication.class);
        UserEntity user = UserEntity.builder().username("validUsername")
                .userId(1L)
                .password("1234567890")
                .enabled(true)
                .isAffiliate(false)
                .role("ROLE_USER")
                .build();
        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .token("refreshToken123")
                .expiryDate(Instant.now().plusSeconds(3600))
                .user(user)
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateJwtToken(authentication)).thenReturn("accessToken123");
        when(userService.getUserEntityByUsername("validUsername")).thenReturn(user);  // CHANGED
        when(jwtTokenProvider.createRefreshToken(user)).thenReturn(refreshToken);
        when(refreshTokenRepository.save(refreshToken)).thenReturn(refreshToken);

        // Act
        UserLoginResponse response = securityService.login(request);

        // Assert
        assertEquals("accessToken123", response.accessToken());
        assertEquals("refreshToken123", response.refreshToken());
    }

    @Test
    void login_invalidPassword_throwsAuthorizationException() {
        // Arrange
        UserLoginRequest request = new UserLoginRequest("validUsername", "invalidPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new AuthorizationException("Invalid username or password", HttpStatus.UNAUTHORIZED));

        // Act & Assert
        AuthorizationException exception = assertThrows(AuthorizationException.class, () -> securityService.login(request));
        assertEquals("Invalid username or password", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
    }

    @Test
    void login_nonexistentUser_throwsAuthorizationException() {
        // Arrange
        UserLoginRequest request = new UserLoginRequest("nonexistentUser", "somePassword");
        Authentication authentication = Mockito.mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userService.getUserEntityByUsername("nonexistentUser"))  // CHANGED
                .thenThrow(new AuthorizationException("User not found", HttpStatus.UNAUTHORIZED));

        // Act & Assert
        AuthorizationException exception = assertThrows(AuthorizationException.class, () -> securityService.login(request));
        assertEquals("User not found", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getHttpStatus());
    }

    @Test
    void login_refreshTokenSaveFails_throwsRuntimeException() {
        // Arrange
        UserLoginRequest request = new UserLoginRequest("validUsername", "validPassword");
        Authentication authentication = Mockito.mock(Authentication.class);
        UserEntity user = UserEntity.builder().username("validUsername").build();
        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .token("refreshToken123")
                .expiryDate(Instant.now().plusSeconds(3600))
                .user(user)  // CHANGED to userId
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateJwtToken(authentication)).thenReturn("accessToken123");
        when(userService.getUserEntityByUsername("validUsername")).thenReturn(user);  // CHANGED
        when(jwtTokenProvider.createRefreshToken(user)).thenReturn(refreshToken);
        when(refreshTokenRepository.save(refreshToken)).thenThrow(new RuntimeException());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> securityService.login(request));
        assertNotNull(exception);
    }
}