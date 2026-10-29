package br.com.TramityRH.TramityRH.service;


import br.com.TramityRH.TramityRH.dto.LoginRequest;
import br.com.TramityRH.TramityRH.dto.LoginResponse;
import br.com.TramityRH.TramityRH.model.UsuarioRhEmpresa;
import br.com.TramityRH.TramityRH.repository.ColaboradorRepository;
import br.com.TramityRH.TramityRH.repository.UsuarioRhEmpresaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class AuthService {

    private final UsuarioRhEmpresaRepository usuarioRhRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UsuarioRhEmpresaRepository usuarioRhRepository,
                       ColaboradorRepository colaboradorRepository,
                       PasswordEncoder passwordEncoder,
                       TokenService tokenService) {
        this.usuarioRhRepository = usuarioRhRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public LoginResponse login(LoginRequest dados) {
        Optional<UsuarioRhEmpresa> rh = usuarioRhRepository.findByEmail(dados.email());
        if (rh.isPresent()) {
            UsuarioRhEmpresa usuario = rh.get();
            if (!usuario.getAtivo() || !passwordEncoder.matches(dados.senha(), usuario.getSenhaHash())) {
                throw credenciaisInvalidas();
            }
            String token = tokenService.gerarToken(usuario.getId(), "RH", usuario.getEmpresa().getId());
            return new LoginResponse(token, "RH", usuario.getNome());
        }

        // SUA PARTE: o mesmo bloco para o colaborador

        throw credenciaisInvalidas();
    }

    private ResponseStatusException credenciaisInvalidas() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos");
    }
}
