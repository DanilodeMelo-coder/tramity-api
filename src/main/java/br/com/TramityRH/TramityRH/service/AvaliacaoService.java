package br.com.TramityRH.TramityRH.service;

import br.com.TramityRH.TramityRH.dto.AvaliarDocumentoRequest;
import br.com.TramityRH.TramityRH.dto.DocumentoResponse;
import br.com.TramityRH.TramityRH.model.DocumentoProcesso;
import br.com.TramityRH.TramityRH.model.Enums.AcaoEvento;
import br.com.TramityRH.TramityRH.model.Enums.ResultadoAvaliacao;
import br.com.TramityRH.TramityRH.model.Enums.StatusDocumento;
import br.com.TramityRH.TramityRH.model.Envio;
import br.com.TramityRH.TramityRH.model.Processo;
import br.com.TramityRH.TramityRH.model.UsuarioRhEmpresa;
import br.com.TramityRH.TramityRH.repository.DocumentoProcessoRepository;
import br.com.TramityRH.TramityRH.repository.EnvioRepository;
import br.com.TramityRH.TramityRH.repository.ProcessoRepository;
import br.com.TramityRH.TramityRH.repository.UsuarioRhEmpresaRepository;
import br.com.TramityRH.TramityRH.security.UsuarioLogado;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import br.com.TramityRH.TramityRH.model.Enums.DirecaoDocumento;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AvaliacaoService {

    private final ProcessoRepository processoRepository;
    private final DocumentoProcessoRepository documentoRepository;
    private final EnvioRepository envioRepository;
    private final UsuarioRhEmpresaRepository usuarioRhRepository;
    private final EventoService eventoService;
    private final DocumentoMapper documentoMapper;

    public AvaliacaoService(ProcessoRepository processoRepository,
                            DocumentoProcessoRepository documentoRepository,
                            EnvioRepository envioRepository,
                            UsuarioRhEmpresaRepository usuarioRhRepository,
                            EventoService eventoService,
                            DocumentoMapper documentoMapper) {
        this.processoRepository = processoRepository;
        this.documentoRepository = documentoRepository;
        this.envioRepository = envioRepository;
        this.usuarioRhRepository = usuarioRhRepository;
        this.eventoService = eventoService;
        this.documentoMapper = documentoMapper;
    }

    @Transactional
    public DocumentoResponse avaliar(UUID idProcesso, UUID idDocumento, AvaliarDocumentoRequest dados, UsuarioLogado usuario) {

        if (!usuario.isRh()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas o RH avalia documentos!");
        }

        boolean recusa = dados.resultado() == ResultadoAvaliacao.RECUSADO;
        String motivo = dados.motivo() == null ? "" : dados.motivo().trim();
        if (recusa && motivo.isEmpty()) { // RN11 aí
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Toda recusa exige motivo");
        }

        Processo processo = processoRepository.findByIdAndEmpresaId(idProcesso, usuario.idEmpresa())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Processo não encontrado"));
        DocumentoProcesso documento = documentoRepository.findByIdAndProcessoId(idDocumento, idProcesso)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Documento não encontrado"));

        if (documento.getDirecao() != DirecaoDocumento.COLABORADOR_ENVIA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Documento da empresa não é avaliado, é aceito pelo colaborador");
        }

        if (documento.getStatus() != StatusDocumento.EM_ANALISE) { // RN10 aqui
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Só é possível avaliar documentos em análise");
        }
        Envio envio = envioRepository
                .findFirstByDocumentoProcessoIdAndResultadoIsNullOrderByEnviadoEmDesc(idDocumento)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Não há envio(s) para analisar"));

        UsuarioRhEmpresa rh = usuarioRhRepository.findById(usuario.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado"));

        envio.setResultado(dados.resultado());
        envio.setMotivoRecusa(recusa ? motivo : null);
        envio.setAvaliadoPor(rh);
        envio.setDataAvaliacao(LocalDateTime.now());
        documento.setStatus(recusa ? StatusDocumento.RECUSADO : StatusDocumento.APROVADO);
        envioRepository.save(envio);
        documentoRepository.save(documento);

        eventoService.registrar(processo, rh.getEmail(),
                recusa ? AcaoEvento.DOCUMENTO_RECUSADO : AcaoEvento.DOCUMENTO_APROVADO);

        return documentoMapper.montar(documento);
    }
}
