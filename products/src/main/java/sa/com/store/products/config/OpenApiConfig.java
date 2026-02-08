package sa.com.store.products.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static String authentication = "Bearer Authentication";
    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(authentication))
                .components(new Components().addSecuritySchemes(authentication, createSecurityScheme()))
                // Remove the hardcoded .servers() for now to let auto-detection work
                // Or ensure it's just the prefix:
                .addServersItem(new Server().url("/products"));
    }


    private SecurityScheme createSecurityScheme() {
        return new SecurityScheme()
                .name(authentication)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
    }
}
