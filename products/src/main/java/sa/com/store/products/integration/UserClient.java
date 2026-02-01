package sa.com.store.products.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import sa.com.store.products.config.FeignClientInterceptor;
import sa.com.store.products.model.UserDTO;

@FeignClient(name = "authorization-service", url = "${AUTH_SERVER}", configuration = FeignClientInterceptor.class)
public interface UserClient {
    @GetMapping("/auth-service/users/me")
    UserDTO getUserById(
                        @RequestHeader("Authorization") String bearerToken);
}