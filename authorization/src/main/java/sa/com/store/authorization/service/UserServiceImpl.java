package sa.com.store.authorization.service;

import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sa.com.store.authorization.controller.dto.*;
import sa.com.store.authorization.data.UserEntity;
import sa.com.store.authorization.mapper.UserMapper;
import sa.com.store.authorization.repository.UserRepository;

import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;



    @Override
    public UserRegistrationResponse registerCustomer(UserRegistrationRequest request) {
        UserEntity userEntity = userMapper.toEntity(request, "ROLE_USER");
        return registerUser(request, userEntity);
    }

    @Override
    public UserRegistrationResponse registerAdmin(UserRegistrationRequest request) {
        UserEntity userEntity = userMapper.toEntity(request, "ROLE_ADMIN");

        return registerUser(request, userEntity);
    }

    @Override
    public UserRegistrationResponse registerAffiliate(UserRegistrationRequest request) {
        UserEntity userEntity = userMapper.toEntity(request, "ROLE_USER");
        userEntity.setAffiliate(true);
        return registerUser(request, userEntity);
    }

    private UserRegistrationResponse registerUser(UserRegistrationRequest request, UserEntity userEntity) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }
        userEntity.setPassword(passwordEncoder.encode(request.password()));
        UserEntity savedUser = userRepository.save(userEntity);
        return new UserRegistrationResponse(String.valueOf(savedUser.getUserId()));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile() {
        UserEntity currentUser = currentUserService.getCurrentUser();
        return userMapper.toResponse(currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('USER_WRITE') or @currentUserService.isCurrentUser(#userId)")
    public UserResponse getUserById(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + userId));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse updateCurrentUserProfile(UserUpdateRequest request) {
        UserEntity currentUser = currentUserService.getCurrentUser();
        
        userMapper.updateEntity(request, currentUser);
        UserEntity updatedUser = userRepository.save(currentUser);
        
        return userMapper.toResponse(updatedUser);
    }

    @Override
    public UserEntity getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found with username: " + username));
    }

    @Override
    public void changeCurrentUserPassword(PasswordChangeRequest request) {
        UserEntity currentUser = currentUserService.getCurrentUser();
        if (!passwordEncoder.matches(request.currentPassword(), currentUser.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        currentUser.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(currentUser);
    }

    @Override
    public void changeCurrentUserEmail(EmailChangeRequest request) {
        UserEntity currentUser = currentUserService.getCurrentUser();
        if (!passwordEncoder.matches(request.password(), currentUser.getPassword())) {
            throw new IllegalArgumentException("Password is incorrect");
        }
        if (userRepository.findByEmail(request.newEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }
        currentUser.setEmail(request.newEmail());
        userRepository.save(currentUser);
    }

    @Override
    public void disableUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + userId));
        
        UserEntity currentUser = currentUserService.getCurrentUser();
        if (user.getUserId().equals(currentUser.getUserId())) {
            throw new IllegalArgumentException("You cannot disable your own account");
        }
        
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    public void enableUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + userId));
        user.setEnabled(true);
        userRepository.save(user);
    }
}
