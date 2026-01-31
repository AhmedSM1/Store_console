package sa.com.store.authorization.service;

import sa.com.store.authorization.controller.dto.*;

public interface UserService {
    UserRegistrationResponse registerCustomer(UserRegistrationRequest request);
    UserRegistrationResponse registerAdmin(UserRegistrationRequest request);
    UserRegistrationResponse registerAffiliate(UserRegistrationRequest request);


    UserResponse getCurrentUserProfile();
    
    UserResponse getUserById(Long userId);
    
    UserResponse updateCurrentUserProfile(UserUpdateRequest request);
    
    void changeCurrentUserPassword(PasswordChangeRequest request);
    
    void changeCurrentUserEmail(EmailChangeRequest request);
    
    void disableUser(Long userId);
    
    void enableUser(Long userId);
}
