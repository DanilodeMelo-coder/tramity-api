package br.com.TramityRH.TramityRH.service;


import br.com.TramityRH.TramityRH.dto.CadastroRequest;
import br.com.TramityRH.TramityRH.dto.CadastroResponse;
import br.com.TramityRH.TramityRH.dto.LoginRequest;
import br.com.TramityRH.TramityRH.dto.LoginResponse;
import br.com.TramityRH.TramityRH.model.Colaborador;
import br.com.TramityRH.TramityRH.model.UsuarioRhEmpresa;
import br.com.TramityRH.TramityRH.repository.ColaboradorRepository;
import br.com.TramityRH.TramityRH.repository.UsuarioRhEmpresaRepository;
import jakarta.transaction.Transactional;
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

        Optional<Colaborador> colab = colaboradorRepository.findByEmail(dados.email());
        if (colab.isPresent()) {
            Colaborador colaborador = colab.get();
            if (!colaborador.getAtivo() || !passwordEncoder.matches(dados.senha(), colaborador.getSenhaHash())) {
                throw credenciaisInvalidas();
            }
            String token = tokenService.gerarToken(colaborador.getId(), "COLABORADOR", null);
            return new LoginResponse(token, "COLABORADOR", colaborador.getNome());
        }

        throw credenciaisInvalidas();
    }

    @Transactional
    public CadastroResponse cadastrar(CadastroRequest dados){

        if (colaboradorRepository.existsByEmail(dados.email())
            || usuarioRhRepository.existsByEmail(dados.email())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail ja cadastrado");
        }

        if (colaboradorRepository.existsByCpf(dados.cpf())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cpf ja cadastrado");
        }

        Colaborador colaborador = new Colaborador();
        colaborador.setNome(dados.nome());
        colaborador.setEmail(dados.email());
        colaborador.setCpf(dados.cpf());
        colaborador.setSenhaHash(passwordEncoder.encode(dados.senha()));

        Colaborador salvo = colaboradorRepository.save(colaborador);
        return new CadastroResponse(salvo.getId(), salvo.getNome(), salvo.getEmail());
    }

    private ResponseStatusException credenciaisInvalidas() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos");
    }
}
