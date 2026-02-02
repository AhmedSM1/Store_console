package sa.com.store.authorization.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import sa.com.store.authorization.controller.dto.*;
import sa.com.store.authorization.data.UserEntity;
import sa.com.store.authorization.mapper.UserMapper;
import sa.com.store.authorization.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRegistrationRequest request;
    private UserEntity userEntity;
    private UserEntity savedUserEntity;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        // Setup test data
        request = UserRegistrationRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .phone("+1234567890")
                .password("password123")
                .build();

        userEntity = UserEntity.builder()
                .username("testuser")
                .email("test@example.com")
                .phonenumber("+1234567890")
                .password("password123")
                .role("ROLE_USER")
                .enabled(true)
                .isAffiliate(false)
                .creationTime(OffsetDateTime.now())
                .build();

        savedUserEntity = UserEntity.builder()
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .phonenumber("+1234567890")
                .password("encoded_password")
                .role("ROLE_USER")
                .enabled(true)
                .isAffiliate(false)
                .creationTime(OffsetDateTime.now())
                .build();

        userResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .phone("+1234567890")
                .build();
    }
    // ================== RegisterCustomer Tests ==================
    @Test
    void registerCustomer_SuccessfulRegistration() {
        // Arrange
        when(userRepository.findByUsername(request.username())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(userMapper.toEntity(request, "ROLE_USER")).thenReturn(userEntity);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded_password");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUserEntity);

        // Act
        UserRegistrationResponse response = userService.registerCustomer(request);

        // Assert
        assertNotNull(response);
        assertEquals("1", response.id());

        verify(userRepository).findByUsername("testuser");
        verify(userRepository).findByEmail("test@example.com");
        verify(userMapper).toEntity(request, "ROLE_USER");
        verify(passwordEncoder).encode("password123");

        ArgumentCaptor<UserEntity> entityCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(entityCaptor.capture());
        UserEntity capturedEntity = entityCaptor.getValue();

        assertEquals("testuser", capturedEntity.getUsername());
        assertEquals("test@example.com", capturedEntity.getEmail());
        assertEquals("+1234567890", capturedEntity.getPhonenumber());
        assertEquals("encoded_password", capturedEntity.getPassword());
        assertFalse(capturedEntity.isAffiliate());
    }

    @Test
    void registerCustomer_UsernameAlreadyExists() {
        // Arrange
        when(userRepository.findByUsername(request.username()))
                .thenReturn(Optional.of(new UserEntity()));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.registerCustomer(request));

        assertEquals("Username already exists", exception.getMessage());

        verify(userRepository).findByUsername("testuser");
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerCustomer_EmailAlreadyExists() {
        // Arrange
        when(userRepository.findByUsername(request.username()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(new UserEntity()));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.registerCustomer(request));

        assertEquals("Email already in use", exception.getMessage());

        verify(userRepository).findByUsername("testuser");
        verify(userRepository).findByEmail("test@example.com");
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any());
    }

    // ================== RegisterAdmin Tests ==================
    @Test
    void registerAdmin_SuccessfulRegistration() {
        // Arrange
        UserEntity adminEntity = UserEntity.builder()
                .username("testuser")
                .email("test@example.com")
                .phonenumber("+1234567890")
                .password("password123")
                .role("ROLE_ADMIN")
                .enabled(true)
                .isAffiliate(false)
                .build();

        UserEntity savedAdminEntity = UserEntity.builder()
                .userId(2L)
                .username("testuser")
                .email("test@example.com")
                .phonenumber("+1234567890")
                .password("encoded_password")
                .role("ROLE_ADMIN")
                .enabled(true)
                .isAffiliate(false)
                .build();

        when(userRepository.findByUsername(request.username())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(userMapper.toEntity(request, "ROLE_ADMIN")).thenReturn(adminEntity);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded_password");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedAdminEntity);

        // Act
        UserRegistrationResponse response = userService.registerAdmin(request);

        // Assert
        assertNotNull(response);
        assertEquals("2", response.id());

        verify(userRepository).findByUsername("testuser");
        verify(userRepository).findByEmail("test@example.com");
        verify(userMapper).toEntity(request, "ROLE_ADMIN");
        verify(passwordEncoder).encode("password123");

        ArgumentCaptor<UserEntity> entityCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(entityCaptor.capture());
        UserEntity capturedEntity = entityCaptor.getValue();

        assertEquals("ROLE_ADMIN", capturedEntity.getRole());
        assertEquals("testuser", capturedEntity.getUsername());
        assertEquals("test@example.com", capturedEntity.getEmail());
        assertFalse(capturedEntity.isAffiliate());
    }

    @Test
    void registerAdmin_WithCustomRole() {
        // Arrange
        
        UserEntity adminEntity = UserEntity.builder()
                .username("testuser")
                .email("test@example.com")
                .phonenumber("+1234567890")
                .password("password123")
                .role("ROLE_ADMIN")
                .enabled(true)
                .isAffiliate(false)
                .build();

        UserEntity savedAdminEntity = UserEntity.builder()
                .userId(3L)
                .username("testuser")
                .email("test@example.com")
                .phonenumber("+1234567890")
                .password("encoded_password")
                .role("ROLE_ADMIN")
                .enabled(true)
                .isAffiliate(false)
                .build();

        when(userRepository.findByUsername(request.username())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(userMapper.toEntity(request, "ROLE_ADMIN")).thenReturn(adminEntity);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded_password");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedAdminEntity);

        // Act
        UserRegistrationResponse response = userService.registerAdmin(request);

        // Assert
        assertNotNull(response);
        verify(userMapper).toEntity(request, "ROLE_ADMIN");
    }

    // ================== RegisterAffiliate Tests ==================
    @Test
    void registerAffiliate_SuccessfulRegistration() {
        // Arrange
        UserEntity affiliateEntity = UserEntity.builder()
                .username("testuser")
                .email("test@example.com")
                .phonenumber("+1234567890")
                .password("password123")
                .role("ROLE_USER")
                .enabled(true)
                .isAffiliate(true)
                .build();

        UserEntity savedAffiliateEntity = UserEntity.builder()
                .userId(4L)
                .username("testuser")
                .email("test@example.com")
                .phonenumber("+1234567890")
                .password("encoded_password")
                .role("ROLE_USER")
                .enabled(true)
                .isAffiliate(true)
                .build();

        when(userRepository.findByUsername(request.username())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(userMapper.toEntity(request, "ROLE_USER")).thenReturn(affiliateEntity);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded_password");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedAffiliateEntity);

        // Act
        UserRegistrationResponse response = userService.registerAffiliate(request);

        // Assert
        assertNotNull(response);
        assertEquals("4", response.id());

        verify(userRepository).findByUsername("testuser");
        verify(userRepository).findByEmail("test@example.com");
        verify(userMapper).toEntity(request, "ROLE_USER");
        verify(passwordEncoder).encode("password123");

        ArgumentCaptor<UserEntity> entityCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(entityCaptor.capture());
        UserEntity capturedEntity = entityCaptor.getValue();

        assertEquals("testuser", capturedEntity.getUsername());
        assertEquals("test@example.com", capturedEntity.getEmail());
        assertEquals("+1234567890", capturedEntity.getPhonenumber());
        assertEquals("encoded_password", capturedEntity.getPassword());
        assertTrue(capturedEntity.isAffiliate());
        assertEquals("ROLE_USER", capturedEntity.getRole());
    }

    @Test
    void registerAffiliate_EmailAlreadyExists() {
        // Arrange
        when(userRepository.findByUsername(request.username()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(new UserEntity()));
        when(userMapper.toEntity(request, "ROLE_USER")).thenReturn(new UserEntity());
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.registerAffiliate(request));

        assertEquals("Email already in use", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    // ================== GetCurrentUserProfile Tests ==================
    @Test
    void getCurrentUserProfile_Success() {
        // Arrange
        when(currentUserService.getCurrentUser()).thenReturn(savedUserEntity);
        when(userMapper.toResponse(savedUserEntity)).thenReturn(userResponse);

        // Act
        UserResponse response = userService.getCurrentUserProfile();

        // Assert
        assertNotNull(response);
        assertEquals("testuser", response.username());
        assertEquals("test@example.com", response.email());

        verify(currentUserService).getCurrentUser();
        verify(userMapper).toResponse(savedUserEntity);
    }

    // ================== GetUserById Tests ==================
    @Test
    void getUserById_Success() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(savedUserEntity));
        when(userMapper.toResponse(savedUserEntity)).thenReturn(userResponse);

        // Act
        UserResponse response = userService.getUserById(userId);

        // Assert
        assertNotNull(response);
        assertEquals("testuser", response.username());
        assertEquals("test@example.com", response.email());

        verify(userRepository).findById(userId);
        verify(userMapper).toResponse(savedUserEntity);
    }

    @Test
    void getUserById_NotFound() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(NoSuchElementException.class,
                () -> userService.getUserById(userId));

        assertEquals("User not found with ID: 999", exception.getMessage());
        verify(userRepository).findById(userId);
        verifyNoInteractions(userMapper);
    }

    // ================== UpdateCurrentUserProfile Tests ==================
    @Test
    void updateCurrentUserProfile_Success() {
        // Arrange
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .firstname("John")
                .lastname("Doe")
                .phone("+1234567891")
                .avatar("avatar.jpg")
                .build();

        UserResponse updatedResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .phone("+1234567891")
                .role("ROLE_USER")
                .enabled(true)
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(savedUserEntity);
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUserEntity);
        when(userMapper.toResponse(savedUserEntity)).thenReturn(updatedResponse);

        // Act
        UserResponse response = userService.updateCurrentUserProfile(updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("testuser", response.username());
        assertEquals("test@example.com", response.email());
        assertEquals("+1234567891", response.phone());
        assertEquals("ROLE_USER", response.role());
        assertTrue(response.enabled());

        verify(currentUserService).getCurrentUser();
        verify(userMapper).updateEntity(updateRequest, savedUserEntity);
        verify(userRepository).save(savedUserEntity);
        verify(userMapper).toResponse(savedUserEntity);
    }

    // ================== ChangeCurrentUserPassword Tests ==================
    @Test
    void changeCurrentUserPassword_Success() {
        // Arrange
        PasswordChangeRequest request = PasswordChangeRequest.builder()
                .currentPassword("password123")
                .newPassword("newpassword456")
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(savedUserEntity);
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
        when(passwordEncoder.encode("newpassword456")).thenReturn("new_encoded_password");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUserEntity);

        // Act
        assertDoesNotThrow(() -> userService.changeCurrentUserPassword(request));

        // Assert
        verify(currentUserService).getCurrentUser();
        verify(passwordEncoder).matches("password123", "encoded_password");
        verify(passwordEncoder).encode("newpassword456");

        ArgumentCaptor<UserEntity> entityCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(entityCaptor.capture());
        assertEquals("new_encoded_password", entityCaptor.getValue().getPassword());
    }

    @Test
    void changeCurrentUserPassword_InvalidCurrentPassword() {
        // Arrange
        PasswordChangeRequest request = PasswordChangeRequest.builder()
                .currentPassword("wrongpassword")
                .newPassword("newpassword456")
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(savedUserEntity);
        when(passwordEncoder.matches("wrongpassword", "encoded_password")).thenReturn(false);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.changeCurrentUserPassword(request));

        assertEquals("Current password is incorrect", exception.getMessage());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    // ================== ChangeCurrentUserEmail Tests ==================
    @Test
    void changeCurrentUserEmail_Success() {
        // Arrange
        EmailChangeRequest request = EmailChangeRequest.builder()
                .newEmail("newemail@example.com")
                .password("password123")
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(savedUserEntity);
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
        when(userRepository.findByEmail("newemail@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUserEntity);

        // Act
        assertDoesNotThrow(() -> userService.changeCurrentUserEmail(request));

        // Assert
        verify(currentUserService).getCurrentUser();
        verify(passwordEncoder).matches("password123", "encoded_password");
        verify(userRepository).findByEmail("newemail@example.com");

        ArgumentCaptor<UserEntity> entityCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(entityCaptor.capture());
        assertEquals("newemail@example.com", entityCaptor.getValue().getEmail());
    }

    @Test
    void changeCurrentUserEmail_InvalidPassword() {
        // Arrange
        EmailChangeRequest request = EmailChangeRequest.builder()
                .newEmail("newemail@example.com")
                .password("wrongpassword")
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(savedUserEntity);
        when(passwordEncoder.matches("wrongpassword", "encoded_password")).thenReturn(false);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.changeCurrentUserEmail(request));

        assertEquals("Password is incorrect", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changeCurrentUserEmail_EmailAlreadyExists() {
        // Arrange
        EmailChangeRequest request = EmailChangeRequest.builder()
                .newEmail("existing@example.com")
                .password("password123")
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(savedUserEntity);
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(new UserEntity()));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.changeCurrentUserEmail(request));

        assertEquals("Email already in use", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

}