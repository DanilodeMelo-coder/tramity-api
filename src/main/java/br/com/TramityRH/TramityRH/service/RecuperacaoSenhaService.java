package br.com.TramityRH.TramityRH.service;

import br.com.TramityRH.TramityRH.dto.*;
import br.com.TramityRH.TramityRH.model.Colaborador;
import br.com.TramityRH.TramityRH.model.RecuperacaoSenha;
import br.com.TramityRH.TramityRH.model.UsuarioRhEmpresa;
import br.com.TramityRH.TramityRH.repository.ColaboradorRepository;
import br.com.TramityRH.TramityRH.repository.RecuperacaoSenhaRepository;
import br.com.TramityRH.TramityRH.repository.UsuarioRhEmpresaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class RecuperacaoSenhaService {

    private static final Logger log = LoggerFactory.getLogger(RecuperacaoSenhaService.class);
    private static final SecureRandom GERADOR = new SecureRandom();

    private final RecuperacaoSenhaRepository recuperacaoRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final UsuarioRhEmpresaRepository usuarioRhRepository;
    private final PasswordEncoder passwordEncoder;

    public RecuperacaoSenhaService(RecuperacaoSenhaRepository recuperacaoRepository,
                                   ColaboradorRepository colaboradorRepository,
                                   UsuarioRhEmpresaRepository usuarioRhRepository,
                                   PasswordEncoder passwordEncoder) {
        this.recuperacaoRepository = recuperacaoRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.usuarioRhRepository = usuarioRhRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public MensagemResponse solicitar(RecuperarSenhaRequest dados) {
        String email = dados.email();

        boolean existe = colaboradorRepository.existsByEmail(email)
                || usuarioRhRepository.existsByEmail(email);

        if (existe) {
            RecuperacaoSenha recuperacao = new RecuperacaoSenha();
            recuperacao.setEmail(email);
            recuperacao.setCodigo(String.format("%06d", GERADOR.nextInt(1_000_000)));
            recuperacao.setDataExpiracao(LocalDateTime.now().plusMinutes(15));
            recuperacaoRepository.save(recuperacao);

            // Sem envio de e-mail real nesta versão: o código aparece no log da API
            log.info("Código de recuperação de senha para {}: {}", email, recuperacao.getCodigo());
        }

        return new MensagemResponse("Se o e-mail estiver cadastrado, você receberá um código de recuperação.");
    }

    @Transactional(readOnly = true)
    public ValidarCodigoResponse validarCodigo(ValidarCodigoRequest dados) {
        RecuperacaoSenha recuperacao = recuperacaoRepository.findFirstByEmailOrderByDataCriacaoDesc(dados.email())
                .filter(r -> r.getCodigo().equals(dados.codigo()))
                .filter(r -> r.getUsadoEm() == null)
                .filter(r -> r.getDataExpiracao().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido ou expirado"));

        return new ValidarCodigoResponse(recuperacao.getId());
    }

    @Transactional
    public void redefinirSenha(NovaSenhaRequest dados) {
        // 1. O token ainda vale?
        RecuperacaoSenha recuperacao = recuperacaoRepository.findById(dados.tokenRedefinicao())
                .filter(r -> r.getUsadoEm() == null)
                .filter(r -> r.getDataExpiracao().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido ou expirado"));

        String novoHash = passwordEncoder.encode(dados.novaSenha());

        // 2. O dono do e-mail: RH primeiro, depois colaborador
        Optional<UsuarioRhEmpresa> rh = usuarioRhRepository.findByEmail(recuperacao.getEmail());
        if (rh.isPresent()) {
            rh.get().setSenhaHash(novoHash);
            usuarioRhRepository.save(rh.get());
        } else {
            Colaborador colaborador = colaboradorRepository.findByEmail(recuperacao.getEmail())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido ou expirado"));
            colaborador.setSenhaHash(novoHash);
            colaboradorRepository.save(colaborador);
        }

        // 3. O token não serve mais (RN18: uso único)
        recuperacao.setUsadoEm(LocalDateTime.now());
        recuperacaoRepository.save(recuperacao);
    }
}