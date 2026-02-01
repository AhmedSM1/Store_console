package sa.com.store.authorization.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import sa.com.store.authorization.controller.dto.UserResponse;
import sa.com.store.authorization.data.AuthenticationUser;
import sa.com.store.authorization.data.RefreshToken;
import sa.com.store.authorization.data.UserEntity;
import sa.com.store.authorization.exception.AuthorizationException;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import static java.time.temporal.ChronoUnit.MINUTES;


@Component
@AllArgsConstructor
public class JWTTokenProvider {
    private JWTProperties jwtProperties;
    private static final Logger logger = LogManager.getLogger(JWTTokenProvider.class);

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getJwtSecret().getBytes());
    }

    public String generateJwtToken(Authentication authentication) {
        AuthenticationUser userPrincipal = (AuthenticationUser) authentication.getPrincipal();
        Map<String, Object> claims = new HashMap<>();
        if (userPrincipal != null) {
            claims.put("authorities", userPrincipal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority) // Extract just the string
                    .collect(Collectors.toList()));
            claims.put("username", userPrincipal.getUsername());
            return buildJwtToken(userPrincipal.getUsername(), claims);
        }else {
            throw new AuthorizationException("Authentication failed", HttpStatus.UNAUTHORIZED);
        }
    }

    public String getUserNameFromJwtToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (ExpiredJwtException | SecurityException | MalformedJwtException | UnsupportedJwtException e) {
            handleJwtException(e);
            throw new AuthorizationException("Invalid JWT", HttpStatus.UNAUTHORIZED);
        }
    }

    public RefreshToken createRefreshToken(UserEntity user) {

        return RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plus(
                        jwtProperties.getRefreshTokenExpiration(),
                        ChronoUnit.DAYS
                ))
                .build();
    }

    public String generateJwtTokenFromUserDetails(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", userDetails.getUsername());
        claims.put("authorities", userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList())
        );
        return buildJwtToken(userDetails.getUsername(), claims);
    }

    private String buildJwtToken(String username, Map<String, Object> claims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(jwtProperties.getJwtExpiration(), MINUTES)))
                .signWith(getSigningKey())
                .claims(claims)
                .compact();
    }

    private List<String> extractAuthoritiesFromJwt(Claims jwt) {
        return ((List<?>) jwt.get("authorities")).stream()
                .map(auth -> ((LinkedHashMap<?, ?>) auth).get("authority").toString())
                .collect(Collectors.toList());
    }

    private void handleJwtException(Exception e) {
        if (e instanceof ExpiredJwtException) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } else if (e instanceof SecurityException) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } else if (e instanceof MalformedJwtException) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } else if (e instanceof UnsupportedJwtException) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        }
    }
}
