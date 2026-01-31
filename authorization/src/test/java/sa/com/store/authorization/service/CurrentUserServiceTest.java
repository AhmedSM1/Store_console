
package sa.com.store.authorization.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import sa.com.store.authorization.data.AuthenticationUser;
import sa.com.store.authorization.data.UserEntity;
import sa.com.store.authorization.exception.UnauthorizedException;
import sa.com.store.authorization.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private CurrentUserService currentUserService;

    private AuthenticationUser authUser;
    private UserEntity customerEntity;


    @BeforeEach
    void setUp() {
        authUser = AuthenticationUser.builder()
                .username("testuser")
                .password("encoded_password")
                .isEnabled(true)
                .authorities(List.of())
                .build();
        customerEntity = UserEntity.builder()
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encoded_password")
                .role("ROLE_USER")
                .enabled(true)
                .build();

    }

    @Test
    void getCurrentUsername_WithAuthenticationUser() {
        // Arrange
        try (MockedStatic<SecurityContextHolder> mockedStatic = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn(authUser);

            // Act
            String result = currentUserService.getCurrentUsername();

            // Assert
            assertEquals("testuser", result);
        }
    }

    @Test
    void getCurrentUsername_WithStringPrincipal() {
        // Arrange
        try (MockedStatic<SecurityContextHolder> mockedStatic = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn("testuser");

            // Act
            String result = currentUserService.getCurrentUsername();

            // Assert
            assertEquals("testuser", result);
        }
    }

    @Test
    void getCurrentUsername_NotAuthenticated() {
        // Arrange
        try (MockedStatic<SecurityContextHolder> mockedStatic = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(false);

            // Act & Assert
            assertThrows(UnauthorizedException.class, () -> currentUserService.getCurrentUsername());
        }
    }

    @Test
    void getCurrentUser_Success() {
        // Arrange
        try (MockedStatic<SecurityContextHolder> mockedStatic = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn(authUser);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(customerEntity));

            // Act
            UserEntity result = currentUserService.getCurrentUser();

            // Assert
            assertNotNull(result);
            assertEquals(1L, result.getUserId());
            assertEquals("testuser", result.getUsername());
            
            verify(userRepository).findByUsername("testuser");
        }
    }

    @Test
    void isAdmin_False() {
        // Arrange
        try (MockedStatic<SecurityContextHolder> mockedStatic = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);

            // Act
            boolean result = currentUserService.isAdmin();

            // Assert
            assertFalse(result);
        }
    }
}
