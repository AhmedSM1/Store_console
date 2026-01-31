package sa.com.store.authorization.service;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sa.com.store.authorization.data.AuthenticationUser;
import sa.com.store.authorization.data.Authority;
import sa.com.store.authorization.data.UserEntity;
import sa.com.store.authorization.exception.AuthorizationException;
import sa.com.store.authorization.repository.UserRepository;

import java.util.List;

@AllArgsConstructor
@Service
public class SecurityUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;
    private RoleService rolesService;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity savedUser =  this.loadUserByUsernameForAuth(username);
        List<Authority> roles = rolesService.getAuthoritiesByRoleName(savedUser.getRole());
        return AuthenticationUser.builder()
                .username(savedUser.getUsername())
                .password(savedUser.getPassword())
                .isEnabled(savedUser.isEnabled())
                .authorities(roles)
                .build();
    }

    private UserEntity loadUserByUsernameForAuth(String username) {
        return userRepository.findByUsername(username).orElseThrow(() ->
                new AuthorizationException(String.format("No User is find with this username: %s", username), HttpStatus.BAD_REQUEST)
        );
    }
}
