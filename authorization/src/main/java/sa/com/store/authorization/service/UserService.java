package sa.com.store.authorization.service;

import sa.com.store.authorization.controller.dto.*;
import sa.com.store.authorization.data.UserEntity;

public interface UserService {
    UserRegistrationResponse registerCustomer(UserRegistrationRequest request);
    UserRegistrationResponse registerAdmin(UserRegistrationRequest request);
    UserRegistrationResponse registerAffiliate(UserRegistrationRequest request);
    UserRegistrationResponse registerEmployee(UserRegistrationRequest request);


    UserResponse getCurrentUserProfile();
    UserResponse getUserByUsername(String username);
    UserResponse updateCurrentUserProfile(UserUpdateRequest request);
    UserEntity getUserEntityByUsername(String username);
    void changeCurrentUserPassword(PasswordChangeRequest request);
    
    void changeCurrentUserEmail(EmailChangeRequest request);

}
