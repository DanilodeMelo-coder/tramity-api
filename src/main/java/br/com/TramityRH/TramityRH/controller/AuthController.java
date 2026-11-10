package br.com.TramityRH.TramityRH.controller;


import br.com.TramityRH.TramityRH.dto.*;
import br.com.TramityRH.TramityRH.service.AuthService;
import br.com.TramityRH.TramityRH.service.RecuperacaoSenhaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final RecuperacaoSenhaService recuperacaoSenhaService;


    public AuthController(AuthService authService,
                          RecuperacaoSenhaService recuperacaoSenhaService){
        this.authService = authService;
        this.recuperacaoSenhaService = recuperacaoSenhaService;
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

    @PostMapping("/recuperar-senha")
    public MensagemResponse recuperarSenha(@RequestBody @Valid RecuperarSenhaRequest dados) {
        return recuperacaoSenhaService.solicitar(dados);
    }
}



