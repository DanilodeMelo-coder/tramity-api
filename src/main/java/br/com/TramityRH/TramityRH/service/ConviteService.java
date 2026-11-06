package br.com.TramityRH.TramityRH.service;

import br.com.TramityRH.TramityRH.dto.ConviteRequest;
import br.com.TramityRH.TramityRH.dto.ConviteResponse;
import br.com.TramityRH.TramityRH.model.Convite;
import br.com.TramityRH.TramityRH.model.Processo;
import br.com.TramityRH.TramityRH.repository.ConviteRepository;
import br.com.TramityRH.TramityRH.repository.ProcessoRepository;
import br.com.TramityRH.TramityRH.security.UsuarioLogado;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ConviteService {

    private static final SecureRandom GERADOR = new SecureRandom();

    private final ConviteRepository conviteRepository;
    private final ProcessoRepository processoRepository;

    public ConviteService(ConviteRepository conviteRepository,
                          ProcessoRepository processoRepository) {
        this.conviteRepository = conviteRepository;
        this.processoRepository = processoRepository;
    }

    @Transactional
    public ConviteResponse criar(UUID idProcesso, ConviteRequest dados, UsuarioLogado usuario) {
        if (!usuario.isRh()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas o RH pode gerar convites");
        }

        Processo processo = processoRepository.findByIdAndEmpresaId(idProcesso, usuario.idEmpresa())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Processo não encontrado"));


        if (processo.getColaborador() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este processo já tem um colaborador");
        }

        Convite convite = new Convite();
        convite.setProcesso(processo);
        convite.setEmail(dados.email());
        convite.setCodigo(gerarCodigoUnico());
        convite.setDataExpiracao(LocalDateTime.now().plusDays(7));
        conviteRepository.save(convite);

        // AUDITORIA: CONVITE_GERADO

        return new ConviteResponse(convite.getCodigo(), convite.getDataExpiracao());
    }

    private String gerarCodigoUnico() {
        String codigo;
        do {
            codigo = String.format("%06d", GERADOR.nextInt(1_000_000));
        } while (conviteRepository.existsByCodigo(codigo));
        return codigo;
    }
}