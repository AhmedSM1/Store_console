package sa.com.store.authorization.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import sa.com.store.authorization.controller.dto.*;
import sa.com.store.authorization.service.SecurityService;

/*
  This rest controller is used  by  store customers to login and refresh tokens.

 */
@RestController
@RequestMapping
@AllArgsConstructor
public class AuthController {

    private SecurityService service;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public UserLoginResponse login(@RequestBody @Valid UserLoginRequest userLoginRequest){
        return service.login(userLoginRequest);
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public TokenRefreshResponse refreshToken(@RequestBody @Valid TokenRefreshRequest request){
        return service.refreshAccessToken(request);
    }



}

