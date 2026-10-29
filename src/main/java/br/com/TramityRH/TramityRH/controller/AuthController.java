package br.com.TramityRH.TramityRH.controller;


import br.com.TramityRH.TramityRH.dto.LoginRequest;
import br.com.TramityRH.TramityRH.dto.LoginResponse;
import br.com.TramityRH.TramityRH.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;


    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest dados){
        return authService.login(dados);
    }
}
