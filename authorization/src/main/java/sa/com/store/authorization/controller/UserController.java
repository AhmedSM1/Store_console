package sa.com.store.authorization.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sa.com.store.authorization.controller.dto.*;
import sa.com.store.authorization.service.UserService;

/*
  This rest controller is used to register and validate Store customers, affiliates and admins.

 */
@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserRegistrationResponse registerCustomer(@Valid @RequestBody UserRegistrationRequest request) {
        return userService.registerCustomer(request);
    }

    @PostMapping("/affiliate")
    @ResponseStatus(HttpStatus.CREATED)
    public UserRegistrationResponse registerAffiliate(@Valid @RequestBody UserRegistrationRequest request) {
        return userService.registerAffiliate(request);
    }

    @PostMapping("/employee")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public UserRegistrationResponse registerEmployee(@Valid @RequestBody UserRegistrationRequest request) {
        return  userService.registerAdmin(request);
    }

    @PostMapping("/admin")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public UserRegistrationResponse registerAdmin(@Valid @RequestBody UserRegistrationRequest request) {
        return  userService.registerAdmin(request);
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser() {
        return userService.getCurrentUserProfile();
    }
    
    @GetMapping("/{userId}")
    public UserResponse getUserById(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }
    
    @PutMapping("/me")
    public UserResponse updateCurrentUser(@Valid @RequestBody UserUpdateRequest request) {
        return userService.updateCurrentUserProfile(request);
    }
    
    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        userService.changeCurrentUserPassword(request);
    }
    
    @PutMapping("/me/email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeEmail(@Valid @RequestBody EmailChangeRequest request) {
        userService.changeCurrentUserEmail(request);
    }
    
    @PutMapping("/{userId}/disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public void disableUser(@PathVariable Long userId) {
        userService.disableUser(userId);
    }
    
    @PutMapping("/{userId}/enable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('USER_DELETE')")
    public void enableUser(@PathVariable Long userId) {
        userService.enableUser(userId);
    }
}
