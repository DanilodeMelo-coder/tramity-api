package br.com.TramityRH.TramityRH.service;

import br.com.TramityRH.TramityRH.dto.AceitarConviteRequest;
import br.com.TramityRH.TramityRH.dto.AceitarConviteResponse;
import br.com.TramityRH.TramityRH.dto.ConviteRequest;
import br.com.TramityRH.TramityRH.dto.ConviteResponse;
import br.com.TramityRH.TramityRH.model.Colaborador;
import br.com.TramityRH.TramityRH.model.Convite;
import br.com.TramityRH.TramityRH.model.Processo;
import br.com.TramityRH.TramityRH.repository.ColaboradorRepository;
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
    private final ColaboradorRepository colaboradorRepository;


    public ConviteService(ConviteRepository conviteRepository,
                          ProcessoRepository processoRepository,
                          ColaboradorRepository colaboradorRepository, ColaboradorRepository colaboradorRepository1) {
        this.conviteRepository = conviteRepository;
        this.processoRepository = processoRepository;
        this.colaboradorRepository = colaboradorRepository1;
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

    @Transactional
    public AceitarConviteResponse aceitar(AceitarConviteRequest dados, UsuarioLogado usuario) {
        // 1. Só o colaborador
        if (usuario.isRh()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas o colaborador pode aceitar convites");
        }

        // 2. O convite existe?
        Convite convite = conviteRepository.findByCodigo(dados.codigo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido"));

        // 3. Ainda vale? (RN01)
        if (convite.getUsadoEm() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este código já foi usado");
        }
        if (convite.getDataExpiracao().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este código expirou");
        }

        // 4. O e-mail da conta é o do convite? (RN02)
        Colaborador colaborador = colaboradorRepository.findById(usuario.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado"));

        if (!colaborador.getEmail().equalsIgnoreCase(convite.getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Este convite foi enviado para outro e-mail");
        }

        // 5. O processo ainda está livre?
        Processo processo = convite.getProcesso();
        if (processo.getColaborador() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este processo já tem um colaborador");
        }

        // 6. Liga o colaborador e marca o convite como usado
        processo.setColaborador(colaborador);
        convite.setUsadoEm(LocalDateTime.now());
        processoRepository.save(processo);
        conviteRepository.save(convite);

        // AUDITORIA: CONVITE_ACEITO

        return new AceitarConviteResponse(processo.getId());
    }

    private String gerarCodigoUnico() {
        String codigo;
        do {
            codigo = String.format("%06d", GERADOR.nextInt(1_000_000));
        } while (conviteRepository.existsByCodigo(codigo));
        return codigo;
    }
}