package sa.com.store.authorization.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sa.com.store.authorization.controller.dto.UserRegistrationRequest;
import sa.com.store.authorization.controller.dto.UserResponse;
import sa.com.store.authorization.controller.dto.UserUpdateRequest;
import sa.com.store.authorization.data.UserEntity;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserMapper Unit Tests")
class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }

    // ================== toEntity Tests ==================
    @Test
    @DisplayName("toEntity_validRequest_createsUserEntity")
    void testToEntityWithValidRequest() {
        // Arrange
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .phone("+1234567890")
                .password("password123")
                .build();

        // Act
        UserEntity entity = userMapper.toEntity(request, "ROLE_USER");

        // Assert
        assertNotNull(entity);
        assertEquals("testuser", entity.getUsername());
        assertEquals("test@example.com", entity.getEmail());
        assertEquals("+1234567890", entity.getPhonenumber());
        assertEquals("password123", entity.getPassword());
        assertEquals("ROLE_USER", entity.getRole());
        assertTrue(entity.isEnabled());
        assertFalse(entity.isAffiliate());
        assertNotNull(entity.getCreationTime());
    }

    @Test
    @DisplayName("toEntity_nullRequest_returnsNull")
    void testToEntityWithNullRequest() {
        // Act
        UserEntity entity = userMapper.toEntity(null, "ROLE_USER");

        // Assert
        assertNull(entity);
    }

    @Test
    @DisplayName("toEntity_differentRoles_createsEntityWithCorrectRole")
    void testToEntityWithDifferentRoles() {
        // Arrange
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .phone("+1234567890")
                .password("password123")
                .build();

        // Act
        UserEntity userEntity = userMapper.toEntity(request, "ROLE_USER");
        UserEntity adminEntity = userMapper.toEntity(request, "ROLE_ADMIN");

        // Assert
        assertEquals("ROLE_USER", userEntity.getRole());
        assertEquals("ROLE_ADMIN", adminEntity.getRole());
        assertEquals("testuser", userEntity.getUsername());
        assertEquals("testuser", adminEntity.getUsername());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ROLE_USER", "ROLE_ADMIN", "ROLE_MODERATOR", "ROLE_AFFILIATE"})
    @DisplayName("toEntity_multipleRoles_allHandledCorrectly")
    void testToEntityWithMultipleRoles(String role) {
        // Arrange
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .phone("+1234567890")
                .password("password123")
                .build();

        // Act
        UserEntity entity = userMapper.toEntity(request, role);

        // Assert
        assertNotNull(entity);
        assertEquals(role, entity.getRole());
        assertEquals("testuser", entity.getUsername());
    }

    @Test
    @DisplayName("toEntity_allFieldsMapCorrectly")
    void testToEntityAllFieldsMapping() {
        // Arrange
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("john_doe")
                .email("john@example.com")
                .phone("+44987654321")
                .password("secure_password_123")
                .build();

        // Act
        UserEntity entity = userMapper.toEntity(request, "ROLE_USER");

        // Assert
        assertEquals("john_doe", entity.getUsername());
        assertEquals("john@example.com", entity.getEmail());
        assertEquals("+44987654321", entity.getPhonenumber());
        assertEquals("secure_password_123", entity.getPassword());
        assertEquals("ROLE_USER", entity.getRole());
        assertTrue(entity.isEnabled());
        assertFalse(entity.isAffiliate());
    }

    @Test
    @DisplayName("toEntity_specialCharactersInFields_preservedCorrectly")
    void testToEntityWithSpecialCharacters() {
        // Arrange
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .username("user@domain.com")
                .email("test+tag@example.com")
                .phone("+1-234-567-8900")
                .password("P@ssw0rd!#$%^&*()")
                .build();

        // Act
        UserEntity entity = userMapper.toEntity(request, "ROLE_USER");

        // Assert
        assertEquals("user@domain.com", entity.getUsername());
        assertEquals("test+tag@example.com", entity.getEmail());
        assertEquals("+1-234-567-8900", entity.getPhonenumber());
        assertEquals("P@ssw0rd!#$%^&*()", entity.getPassword());
    }

    // ================== toResponse Tests ==================
    @Test
    @DisplayName("toResponse_validEntity_createsUserResponse")
    void testToResponseWithValidEntity() {
        // Arrange
        UserEntity entity = UserEntity.builder()
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .phonenumber("+1234567890")
                .role("ROLE_USER")
                .enabled(true)
                .isAffiliate(false)
                .creationTime(OffsetDateTime.now())
                .build();

        // Act
        UserResponse response = userMapper.toResponse(entity);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("testuser", response.username());
        assertEquals("test@example.com", response.email());
        assertEquals("+1234567890", response.phone());
        assertEquals("ROLE_USER", response.role());
        assertTrue(response.enabled());
    }

    @Test
    @DisplayName("toResponse_nullEntity_returnsNull")
    void testToResponseWithNullEntity() {
        // Act
        UserResponse response = userMapper.toResponse(null);

        // Assert
        assertNull(response);
    }

    @Test
    @DisplayName("toResponse_disabledUser_returnsResponseWithEnabledFalse")
    void testToResponseWithDisabledUser() {
        // Arrange
        UserEntity entity = UserEntity.builder()
                .userId(2L)
                .username("disabled_user")
                .email("disabled@example.com")
                .phonenumber("+1234567890")
                .role("ROLE_USER")
                .enabled(false)
                .isAffiliate(false)
                .creationTime(OffsetDateTime.now())
                .build();

        // Act
        UserResponse response = userMapper.toResponse(entity);

        // Assert
        assertNotNull(response);
        assertEquals(2L, response.id());
        assertFalse(response.enabled());
        assertEquals("disabled_user", response.username());
    }

    @Test
    @DisplayName("toResponse_adminUser_returnsResponseWithAdminRole")
    void testToResponseWithAdminUser() {
        // Arrange
        UserEntity entity = UserEntity.builder()
                .userId(3L)
                .username("admin_user")
                .email("admin@example.com")
                .phonenumber("+1234567890")
                .role("ROLE_ADMIN")
                .enabled(true)
                .isAffiliate(false)
                .creationTime(OffsetDateTime.now())
                .build();

        // Act
        UserResponse response = userMapper.toResponse(entity);

        // Assert
        assertEquals("ROLE_ADMIN", response.role());
        assertEquals(3L, response.id());
        assertTrue(response.enabled());
    }

    @Test
    @DisplayName("toResponse_allFieldsMappedCorrectly")
    void testToResponseAllFieldsMapping() {
        // Arrange
        UserEntity entity = UserEntity.builder()
                .userId(99L)
                .username("complete_user")
                .email("complete@example.com")
                .phonenumber("+9876543210")
                .role("ROLE_USER")
                .enabled(true)
                .isAffiliate(true)
                .creationTime(OffsetDateTime.now())
                .build();

        // Act
        UserResponse response = userMapper.toResponse(entity);

        // Assert
        assertEquals(99L, response.id());
        assertEquals("complete_user", response.username());
        assertEquals("complete@example.com", response.email());
        assertEquals("+9876543210", response.phone());
        assertEquals("ROLE_USER", response.role());
        assertTrue(response.enabled());
    }

    // ================== updateEntity Tests ==================
    @Test
    @DisplayName("updateEntity_validRequest_updatesPhoneNumber")
    void testUpdateEntityWithValidRequest() {
        // Arrange
        UserEntity entity = UserEntity.builder()
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .phonenumber("+1111111111")
                .role("ROLE_USER")
                .enabled(true)
                .isAffiliate(false)
                .creationTime(OffsetDateTime.now())
                .build();

        UserUpdateRequest request = UserUpdateRequest.builder()
                .phone("+2222222222")
                .firstname("John")
                .lastname("Doe")
                .avatar("avatar.jpg")
                .build();

        // Act
        userMapper.updateEntity(request, entity);

        // Assert
        assertEquals("+2222222222", entity.getPhonenumber());
        assertEquals("testuser", entity.getUsername());
        assertEquals("test@example.com", entity.getEmail());
    }

    @Test
    @DisplayName("updateEntity_nullRequest_doesNotUpdateEntity")
    void testUpdateEntityWithNullRequest() {
        // Arrange
        UserEntity entity = UserEntity.builder()
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .phonenumber("+1111111111")
                .role("ROLE_USER")
                .enabled(true)
                .isAffiliate(false)
                .creationTime(OffsetDateTime.now())
                .build();

        // Act
        userMapper.updateEntity(null, entity);

        // Assert
        assertEquals("+1111111111", entity.getPhonenumber());
        assertEquals("testuser", entity.getUsername());
    }

    @Test
    @DisplayName("updateEntity_nullEntity_noExceptionThrown")
    void testUpdateEntityWithNullEntity() {
        // Arrange
        UserUpdateRequest request = UserUpdateRequest.builder()
                .phone("+2222222222")
                .build();

        // Act & Assert
        assertDoesNotThrow(() -> userMapper.updateEntity(request, null));
    }

    @Test
    @DisplayName("updateEntity_nullPhoneInRequest_doesNotUpdatePhone")
    void testUpdateEntityWithNullPhoneInRequest() {
        // Arrange
        UserEntity entity = UserEntity.builder()
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .phonenumber("+1111111111")
                .role("ROLE_USER")
                .enabled(true)
                .isAffiliate(false)
                .creationTime(OffsetDateTime.now())
                .build();

        UserUpdateRequest request = UserUpdateRequest.builder()
                .firstname("John")
                .lastname("Doe")
                .build();

        // Act
        userMapper.updateEntity(request, entity);

        // Assert
        assertEquals("+1111111111", entity.getPhonenumber());
    }

    @Test
    @DisplayName("updateEntity_phoneFieldUpdated_otherFieldsPreserved")
    void testUpdateEntityPreservesOtherFields() {
        // Arrange
        UserEntity entity = UserEntity.builder()
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .phonenumber("+1111111111")
                .role("ROLE_USER")
                .enabled(true)
                .isAffiliate(false)
                .creationTime(OffsetDateTime.now())
                .build();

        UserUpdateRequest request = UserUpdateRequest.builder()
                .phone("+9999999999")
                .build();

        // Act
        userMapper.updateEntity(request, entity);

        // Assert
        assertEquals("+9999999999", entity.getPhonenumber());
        assertEquals(1L, entity.getUserId());
        assertEquals("testuser", entity.getUsername());
        assertEquals("test@example.com", entity.getEmail());
        assertEquals("ROLE_USER", entity.getRole());
        assertTrue(entity.isEnabled());
        assertFalse(entity.isAffiliate());
    }

    @ParameterizedTest
    @ValueSource(strings = {"+1234567890", "+44987654321", "+86138000000000", "+919876543210"})
    @DisplayName("updateEntity_multiplePhoneNumbers_allUpdatedCorrectly")
    void testUpdateEntityWithMultiplePhoneNumbers(String phoneNumber) {
        // Arrange
        UserEntity entity = UserEntity.builder()
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .phonenumber("+1111111111")
                .role("ROLE_USER")
                .enabled(true)
                .isAffiliate(false)
                .creationTime(OffsetDateTime.now())
                .build();

        UserUpdateRequest request = UserUpdateRequest.builder()
                .phone(phoneNumber)
                .build();

        // Act
        userMapper.updateEntity(request, entity);

        // Assert
        assertEquals(phoneNumber, entity.getPhonenumber());
    }

    @Test
    @DisplayName("updateEntity_requestWithAllFieldsNull_entityUnchanged")
    void testUpdateEntityWithAllNullFields() {
        // Arrange
        UserEntity entity = UserEntity.builder()
                .userId(1L)
                .username("testuser")
                .email("test@example.com")
                .phonenumber("+1111111111")
                .role("ROLE_USER")
                .enabled(true)
                .isAffiliate(false)
                .creationTime(OffsetDateTime.now())
                .build();

        UserUpdateRequest request = UserUpdateRequest.builder().build();

        // Act
        userMapper.updateEntity(request, entity);

        // Assert
        assertEquals("+1111111111", entity.getPhonenumber());
        assertEquals("testuser", entity.getUsername());
    }
}
