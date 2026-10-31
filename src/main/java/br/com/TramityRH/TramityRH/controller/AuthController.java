package br.com.TramityRH.TramityRH.controller;


import br.com.TramityRH.TramityRH.dto.CadastroRequest;
import br.com.TramityRH.TramityRH.dto.CadastroResponse;
import br.com.TramityRH.TramityRH.dto.LoginRequest;
import br.com.TramityRH.TramityRH.dto.LoginResponse;
import br.com.TramityRH.TramityRH.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public CadastroResponse cadastrar(@RequestBody @Valid CadastroRequest dados){
        return authService.cadastrar(dados);
    }
}



