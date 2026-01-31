package sa.com.store.products.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import sa.com.store.products.config.FeignClientInterceptor;
import sa.com.store.products.model.UserDTO;

@FeignClient(name = "authorization-service", url = "${AUTH_SERVICE_URL:http://authorization-service:8070}", configuration = FeignClientInterceptor.class)
public interface UserClient {
    @GetMapping("/api/users/{id}")
    UserDTO getUserById(@PathVariable("id") String userId,
                        @RequestHeader("Authorization") String bearerToken);
}