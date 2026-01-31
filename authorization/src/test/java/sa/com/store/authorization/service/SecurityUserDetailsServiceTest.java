
package sa.com.store.authorization.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import sa.com.store.authorization.data.Authority;
import sa.com.store.authorization.data.AuthenticationUser;
import sa.com.store.authorization.data.UserEntity;
import sa.com.store.authorization.exception.AuthorizationException;
import sa.com.store.authorization.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityUserDetailsService Unit Tests")
class SecurityUserDetailsServiceTest {

    @InjectMocks
    private SecurityUserDetailsService securityUserDetailsService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    private UserEntity testUser;
    private List<Authority> testAuthorities;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encoded_password_123")
                .phonenumber("+1234567890")
                .role("ROLE_USER")
                .enabled(true)
                .isAffiliate(false)
                .creationTime(OffsetDateTime.now())
                .build();

        testAuthorities = List.of(
                Authority.builder()
                        .authorityId(1L)
                        .authority("ROLE_USER")
                        .build()
        );
    }

    // ================== LoadUserByUsername Tests ==================
    @Test
    @DisplayName("loadUserByUsername_userExists_returnsAuthenticationUser")
    void testLoadUserByUsernameSuccess() {
        // Arrange
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(roleService.getAuthoritiesByRoleName("ROLE_USER"))
                .thenReturn(testAuthorities);

        // Act
        UserDetails userDetails = securityUserDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails);
        assertInstanceOf(AuthenticationUser.class, userDetails);

        AuthenticationUser authUser = (AuthenticationUser) userDetails;
        assertEquals("testuser", authUser.getUsername());
        assertEquals("encoded_password_123", authUser.getPassword());
        assertTrue(authUser.isEnabled());

        Collection<?> authorities = authUser.getAuthorities();
        assertNotNull(authorities);
        assertEquals(1, authorities.size());

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(roleService, times(1)).getAuthoritiesByRoleName("ROLE_USER");
    }

    @Test
    @DisplayName("loadUserByUsername_userNotFound_throwsAuthorizationException")
    void testLoadUserByUsernameNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistent"))
                .thenReturn(Optional.empty());

        // Act & Assert
        AuthorizationException exception = assertThrows(AuthorizationException.class,
                () -> securityUserDetailsService.loadUserByUsername("nonexistent"));

        assertTrue(exception.getMessage().contains("No User is find with this username: nonexistent"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());

        verify(userRepository, times(1)).findByUsername("nonexistent");
        verifyNoInteractions(roleService);
    }

    @Test
    @DisplayName("loadUserByUsername_disabledUser_returnsUserWithEnabledFalse")
    void testLoadUserByUsernameDisabledUser() {
        // Arrange
        UserEntity disabledUser = UserEntity.builder()
                .userId(2L)
                .username("disabled_user")
                .email("disabled@example.com")
                .password("encoded_password")
                .phonenumber("+1234567890")
                .role("ROLE_USER")
                .enabled(false)
                .isAffiliate(false)
                .creationTime(OffsetDateTime.now())
                .build();

        when(userRepository.findByUsername("disabled_user"))
                .thenReturn(Optional.of(disabledUser));
        when(roleService.getAuthoritiesByRoleName("ROLE_USER"))
                .thenReturn(testAuthorities);

        // Act
        UserDetails userDetails = securityUserDetailsService.loadUserByUsername("disabled_user");

        // Assert
        assertNotNull(userDetails);
        assertFalse(userDetails.isEnabled());
        assertEquals("disabled_user", userDetails.getUsername());

        verify(userRepository, times(1)).findByUsername("disabled_user");
        verify(roleService, times(1)).getAuthoritiesByRoleName("ROLE_USER");
    }

    @Test
    @DisplayName("loadUserByUsername_adminUser_returnsAuthenticationUserWithAdminRole")
    void testLoadUserByUsernameAdminUser() {
        // Arrange
        UserEntity adminUser = UserEntity.builder()
                .userId(3L)
                .username("admin_user")
                .email("admin@example.com")
                .password("admin_password")
                .phonenumber("+1234567890")
                .role("ROLE_ADMIN")
                .enabled(true)
                .isAffiliate(false)
                .creationTime(OffsetDateTime.now())
                .build();

        List<Authority> adminAuthorities = List.of(
                Authority.builder()
                        .authorityId(2L)
                        .authority("ROLE_ADMIN")
                        .build()
        );

        when(userRepository.findByUsername("admin_user"))
                .thenReturn(Optional.of(adminUser));
        when(roleService.getAuthoritiesByRoleName("ROLE_ADMIN"))
                .thenReturn(adminAuthorities);

        // Act
        UserDetails userDetails = securityUserDetailsService.loadUserByUsername("admin_user");

        // Assert
        assertNotNull(userDetails);
        assertEquals("admin_user", userDetails.getUsername());
        assertEquals("admin_password", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());

        Collection<?> authorities = userDetails.getAuthorities();
        assertEquals(1, authorities.size());

        verify(userRepository, times(1)).findByUsername("admin_user");
        verify(roleService, times(1)).getAuthoritiesByRoleName("ROLE_ADMIN");
    }

    @Test
    @DisplayName("loadUserByUsername_userWithMultipleAuthorities_returnAllAuthorities")
    void testLoadUserByUsernameWithMultipleAuthorities() {
        // Arrange
        List<Authority> multipleAuthorities = List.of(
                Authority.builder().authorityId(1L).authority("ROLE_USER").build(),
                Authority.builder().authorityId(2L).authority("ROLE_MODERATOR").build()
        );

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(roleService.getAuthoritiesByRoleName("ROLE_USER"))
                .thenReturn(multipleAuthorities);

        // Act
        UserDetails userDetails = securityUserDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails);
        Collection<?> authorities = userDetails.getAuthorities();
        assertEquals(2, authorities.size());

        verify(roleService, times(1)).getAuthoritiesByRoleName("ROLE_USER");
    }

    @ParameterizedTest
    @ValueSource(strings = {"user1", "user2", "admin_user", "test_user"})
    @DisplayName("loadUserByUsername_multipleUsernames_allHandledCorrectly")
    void testLoadUserByUsernameMultipleUsers(String username) {
        // Arrange
        UserEntity user = UserEntity.builder()
                .userId(1L)
                .username(username)
                .email(username + "@example.com")
                .password("password_" + username)
                .phonenumber("+1234567890")
                .role("ROLE_USER")
                .enabled(true)
                .isAffiliate(false)
                .creationTime(OffsetDateTime.now())
                .build();

        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(user));
        when(roleService.getAuthoritiesByRoleName("ROLE_USER"))
                .thenReturn(testAuthorities);

        // Act
        UserDetails userDetails = securityUserDetailsService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals("password_" + username, userDetails.getPassword());

        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("loadUserByUsername_emptyStringUsername_throwsAuthorizationException")
    void testLoadUserByUsernameEmptyString() {
        // Arrange
        when(userRepository.findByUsername(""))
                .thenReturn(Optional.empty());

        // Act & Assert
        AuthorizationException exception = assertThrows(AuthorizationException.class,
                () -> securityUserDetailsService.loadUserByUsername(""));

        assertTrue(exception.getMessage().contains("No User is find with this username"));
        verify(userRepository, times(1)).findByUsername("");
    }

    @Test
    @DisplayName("loadUserByUsername_caseInsensitiveUsername_handledByRepository")
    void testLoadUserByUsernameCaseSensitivity() {
        // Arrange
        when(userRepository.findByUsername("TestUser"))
                .thenReturn(Optional.of(testUser));
        when(roleService.getAuthoritiesByRoleName("ROLE_USER"))
                .thenReturn(testAuthorities);

        // Act
        UserDetails userDetails = securityUserDetailsService.loadUserByUsername("TestUser");

        // Assert
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());

        verify(userRepository, times(1)).findByUsername("TestUser");
    }

    @Test
    @DisplayName("loadUserByUsername_affiliateUser_returnsCorrectUserDetails")
    void testLoadUserByUsernameAffiliateUser() {
        // Arrange
        UserEntity affiliateUser = UserEntity.builder()
                .userId(4L)
                .username("affiliate_user")
                .email("affiliate@example.com")
                .password("affiliate_password")
                .phonenumber("+1234567890")
                .role("ROLE_USER")
                .enabled(true)
                .isAffiliate(true)
                .creationTime(OffsetDateTime.now())
                .build();

        when(userRepository.findByUsername("affiliate_user"))
                .thenReturn(Optional.of(affiliateUser));
        when(roleService.getAuthoritiesByRoleName("ROLE_USER"))
                .thenReturn(testAuthorities);

        // Act
        UserDetails userDetails = securityUserDetailsService.loadUserByUsername("affiliate_user");

        // Assert
        assertNotNull(userDetails);
        assertEquals("affiliate_user", userDetails.getUsername());
        assertTrue(userDetails.isEnabled());

        verify(userRepository, times(1)).findByUsername("affiliate_user");
        verify(roleService, times(1)).getAuthoritiesByRoleName("ROLE_USER");
    }

    @Test
    @DisplayName("loadUserByUsername_userDetailsAccountMethods_allReturnCorrectValues")
    void testLoadUserByUsernameAccountMethods() {
        // Arrange
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(roleService.getAuthoritiesByRoleName("ROLE_USER"))
                .thenReturn(testAuthorities);

        // Act
        UserDetails userDetails = securityUserDetailsService.loadUserByUsername("testuser");

        // Assert
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());

        assertFalse(userDetails.isAccountNonExpired() && !testUser.isEnabled());
    }

    @Test
    @DisplayName("loadUserByUsername_getAuthoritiesNotNull")
    void testLoadUserByUsernameAuthoritiesNotNull() {
        // Arrange
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(roleService.getAuthoritiesByRoleName("ROLE_USER"))
                .thenReturn(testAuthorities);

        // Act
        UserDetails userDetails = securityUserDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails.getAuthorities());
        assertFalse(userDetails.getAuthorities().isEmpty());
    }
}
