package sa.com.store.authorization.mapper;

import org.springframework.stereotype.Component;
import sa.com.store.authorization.controller.dto.UserRegistrationRequest;
import sa.com.store.authorization.controller.dto.UserResponse;
import sa.com.store.authorization.controller.dto.UserUpdateRequest;
import sa.com.store.authorization.data.UserEntity;

import java.time.OffsetDateTime;

@Component
public class UserMapper {

    public UserEntity toEntity(UserRegistrationRequest request,String role) {
        if (request == null) {
            return null;
        }

        return UserEntity.builder()
                .username(request.username())
                .email(request.email())
                .phonenumber(request.phone())
                .password(request.password())
                .enabled(true)
                .role(role)
                .creationTime(OffsetDateTime.now())
                .isAffiliate(false)
                .build();
    }

    public UserResponse toResponse(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        return UserResponse.builder()
                .id(entity.getUserId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .phone(entity.getPhonenumber())
                .role(entity.getRole())
                .enabled(entity.isEnabled())
                .build();
    }

    public void updateEntity(UserUpdateRequest request, UserEntity entity) {
        if (request == null || entity == null) {
            return;
        }
        // same behavior as NullValuePropertyMappingStrategy.IGNORE
        if (request.phone() != null) {
            entity.setPhonenumber(request.phone());
        }
    }
}
