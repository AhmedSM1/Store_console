package sa.com.store.authorization.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.security")
@Getter
@Setter
public class JWTProperties {
    private String jwtSecret;
    private long jwtExpiration;
    private int refreshTokenExpiration;

}
