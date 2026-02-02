package sa.com.store.products.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import sa.com.store.products.entity.Order;

import java.util.Collection;


@Aspect
@Configuration
@Slf4j
public class AuthorizedAspect {

    public static final String ADMIN_DASHBOARD_ACCESS = "ADMIN_DASHBOARD_ACCESS";
    @After("@annotation(Authorized) && args(orderid, ..)")
    public void authorizedToAccess(JoinPoint joinPoint, Order order) {
        String currentUsername = getCurrentUsername();
        boolean hasAccessPermission = hasAccessPermission(currentUsername, order);

        if (!hasAccessPermission) {
            throw new IllegalArgumentException("Not authorized to view this order");
        }
    }

    private String getCurrentUsername() {
        Authentication authentication = getAuthentication();
        String username = authentication.getName();
        log.info("Current User Authentication Details: {}", username);
        return username;
    }

    private boolean hasAccessPermission(String currentUsername, Order order) {
        Authentication authentication = getAuthentication();
        if (authentication != null) {
            boolean isAdmin = isUserAdmin(authentication);
            boolean isOrderOwner = order.getUsername().equals(currentUsername);
            return isAdmin || isOrderOwner;
        }
        return false;
    }

    private static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean isUserAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(ADMIN_DASHBOARD_ACCESS));
    }
}
