package sa.com.store.authorization.service;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import sa.com.store.authorization.data.Authority;
import sa.com.store.authorization.data.Role;
import sa.com.store.authorization.exception.AuthorizationException;
import sa.com.store.authorization.repository.RoleRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class RoleService {
    private RoleRepository roleRepository;


    /**
     * Retrieves the list of authorities associated with a role name.
     *
     * @param roleName The name of the role to retrieve authorities for
     * @return List of authorities for the specified role
     * @throws AuthorizationException if the specified role doesn't exist or has no authorities
     */
    public List<Authority> getAuthoritiesByRoleName(String roleName) {
        var role = findRoleOrThrow(roleName);
        var authorities = role.getAuthorities();

        if (authorities == null || authorities.isEmpty()) {
            throw new AuthorizationException(
                    String.format("Authorization failed: Role '%s' has no authorities assigned", roleName),
                    HttpStatus.FORBIDDEN
            );
        }
        return authorities.stream().toList();
    }

    /**
     * Helper method to find a role by name or throw an exception.
     *
     * @param roleName The name of the role to find
     * @return The found role entity
     * @throws AuthorizationException if the role doesn't exist
     */
    private Role findRoleOrThrow(String roleName) {
        return roleRepository.findRoleByName(roleName)
                .orElseThrow(() ->
                        new AuthorizationException(
                                String.format("Authorization failed: Required role '%s' no longer exists", roleName),
                                HttpStatus.FORBIDDEN
                        )
                );
    }
    
}
