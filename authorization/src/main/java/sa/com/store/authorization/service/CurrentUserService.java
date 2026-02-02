package sa.com.store.authorization.service;

import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import sa.com.store.authorization.data.UserEntity;
import sa.com.store.authorization.exception.UnauthorizedException;
import sa.com.store.authorization.repository.UserRepository;

@Service
@AllArgsConstructor
public class CurrentUserService {
    
    private final UserRepository userRepository;

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        } else if (principal instanceof String ) {
            return  String.valueOf(principal);
        }
        throw new UnauthorizedException("User details not available");
    }
    
    /**
     * Gets the current user entity from the database
     * @return the current user entity
     * @throws UnauthorizedException if no authenticated user is found or the user doesn't exist in the database
     */
    public UserEntity getCurrentUser() {
        String username = getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found in the system"));
    }
    



}
