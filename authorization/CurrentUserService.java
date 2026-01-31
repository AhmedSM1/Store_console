
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
    
    /**
     * Gets the username of the currently authenticated user
     * @return the username as a string
     * @throws UnauthorizedException if no authenticated user is found
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
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
    
    /**
     * Checks if the authenticated user is an admin
     * @return true if the user has ROLE_ADMIN, false otherwise
     */
    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
    
    /**
     * Checks if the authenticated user has the specified userId
     * @param userId the user ID to check against
     * @return true if the current user has the specified ID
     */
    public boolean isCurrentUser(Long userId) {
        if (userId == null) {
            return false;
        }
        
        try {
            UserEntity currentUser = getCurrentUser();
            return currentUser.getUserId().equals(userId);
        } catch (UnauthorizedException e) {
            return false;
        }
    }
    
    /**
     * Checks if the current user is authorized to perform actions on the specified user
     * This means either the user is acting on their own account or they are an admin
     * @param userId the user ID to check authorization for
     * @return true if authorized, false otherwise
     */
    public boolean isAuthorizedForUser(Long userId) {
        return isCurrentUser(userId) || isAdmin();
    }
}
