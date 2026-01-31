package sa.com.store.products.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtConfig {


    private  String secret;

    public JwtConfig(@Value("${app.security.jwtSecret}") String secret){
        this.secret = secret;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        var secretKey = new SecretKeySpec(keyBytes, "HmacSHA512");
        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS512) // <--- CRITICAL CHANGE
                .build();
    }
}