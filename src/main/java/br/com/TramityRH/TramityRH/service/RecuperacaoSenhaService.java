package br.com.TramityRH.TramityRH.service;

import br.com.TramityRH.TramityRH.dto.MensagemResponse;
import br.com.TramityRH.TramityRH.dto.RecuperarSenhaRequest;
import br.com.TramityRH.TramityRH.model.RecuperacaoSenha;
import br.com.TramityRH.TramityRH.repository.ColaboradorRepository;
import br.com.TramityRH.TramityRH.repository.RecuperacaoSenhaRepository;
import br.com.TramityRH.TramityRH.repository.UsuarioRhEmpresaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.LocalDateTime;


@Service
public class RecuperacaoSenhaService {

    private static final Logger log = LoggerFactory.getLogger(RecuperacaoSenhaService.class);
    private static final SecureRandom GERADOR = new SecureRandom();

    private final RecuperacaoSenhaRepository recuperacaoRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final UsuarioRhEmpresaRepository usuarioRhRepository;

    public RecuperacaoSenhaService(RecuperacaoSenhaRepository recuperacaoRepository,
                                   ColaboradorRepository colaboradorRepository,
                                   UsuarioRhEmpresaRepository usuarioRhRepository) {
        this.recuperacaoRepository = recuperacaoRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.usuarioRhRepository = usuarioRhRepository;
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
}