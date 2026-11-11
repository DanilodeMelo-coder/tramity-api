package br.com.TramityRH.TramityRH.service;

import br.com.TramityRH.TramityRH.dto.DocumentoResponse;
import br.com.TramityRH.TramityRH.model.Colaborador;
import br.com.TramityRH.TramityRH.model.DocumentoProcesso;
import br.com.TramityRH.TramityRH.model.Enums.AcaoEvento;
import br.com.TramityRH.TramityRH.model.Enums.DirecaoDocumento;
import br.com.TramityRH.TramityRH.model.Enums.StatusDocumento;
import br.com.TramityRH.TramityRH.model.Enums.TipoProcesso;
import br.com.TramityRH.TramityRH.model.Envio;
import br.com.TramityRH.TramityRH.model.Processo;
import br.com.TramityRH.TramityRH.model.UsuarioRhEmpresa;
import br.com.TramityRH.TramityRH.repository.ColaboradorRepository;
import br.com.TramityRH.TramityRH.repository.DocumentoProcessoRepository;
import br.com.TramityRH.TramityRH.repository.EnvioRepository;
import br.com.TramityRH.TramityRH.repository.ProcessoRepository;
import br.com.TramityRH.TramityRH.repository.UsuarioRhEmpresaRepository;
import br.com.TramityRH.TramityRH.security.UsuarioLogado;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DemissaoService {
    private final ProcessoRepository processoRepository;
    private final DocumentoProcessoRepository documentoProcessoRepository;
    private final EnvioRepository envioRepository;
    private final UsuarioRhEmpresaRepository usuarioRhEmpresaRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final ArmazenamentoService armazenamentoService;
    private final EventoService eventoService;

    public DemissaoService(
            ProcessoRepository processoRepository,
            DocumentoProcessoRepository documentoProcessoRepository,
            EnvioRepository envioRepository,
            UsuarioRhEmpresaRepository usuarioRhEmpresaRepository,
            ColaboradorRepository colaboradorRepository,
            ArmazenamentoService armazenamentoService,
            EventoService eventoService) {
        this.processoRepository = processoRepository;
        this.documentoProcessoRepository = documentoProcessoRepository;
        this.envioRepository = envioRepository;
        this.usuarioRhEmpresaRepository = usuarioRhEmpresaRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.armazenamentoService = armazenamentoService;
        this.eventoService = eventoService;
    }
    // RF14 aqui
    @Transactional
    public DocumentoResponse enviarArquivo(UUID idProcesso, UUID idDocumento, MultipartFile arquivo, UsuarioLogado usuario) {
        if (!usuario.isRh()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas o RH envia arquivos da empresa");
        }

        Processo processo = processoRepository.findByIdAndEmpresaId(idProcesso, usuario.idEmpresa())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Processo não encontrado"));
        if (processo.getTipo() != TipoProcesso.DEMISSAO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Só processos de demissão têm arquivo da empresa");
        }
        if (processo.getDataConclusao() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Processo já concluído");
        }
        if (processo.getColaborador() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O processo ainda não tem colaborador vinculado");
        }

        DocumentoProcesso documento = documentoProcessoRepository.findByIdAndProcessoId(idDocumento, idProcesso)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Documento não encontrado"));
        if (documento.getDirecao() != DirecaoDocumento.EMPRESA_ENVIA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este documento é enviado pelo colaborador");
        }
        if (documento.getStatus() == StatusDocumento.ACEITO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Documento já aceito, não pode ser substituído");
        }

        UsuarioRhEmpresa rh = usuarioRhEmpresaRepository.findById(usuario.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado"));

        String caminho = armazenamentoService.salvar(arquivo, processo, documento.getTipoDocumento().getNome());

        Envio envio = new Envio();
        envio.setDocumentoProcesso(documento);
        envio.setArquivoUrl(caminho);
        envioRepository.save(envio);

        documento.setStatus(StatusDocumento.EM_ANALISE);
        documentoProcessoRepository.save(documento);

        eventoService.registrar(processo, rh.getEmail(), AcaoEvento.DOCUMENTO_ENVIADO);
        return resposta(documento);
    }

    // RF15 e RN17: o colaborador aceita, e o aceite não pode ser desfeito
    @Transactional
    public DocumentoResponse aceitar(UUID idProcesso, UUID idDocumento, UsuarioLogado usuario) {
        if (usuario.isRh()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas o colaborador dá o aceite");
        }

        Processo processo = processoRepository.findByIdAndColaboradorId(idProcesso, usuario.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Processo não encontrado"));
        if (processo.getTipo() != TipoProcesso.DEMISSAO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Aceite só existe na demissão");
        }
        if (processo.getDataConclusao() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Processo já concluído");
        }

        DocumentoProcesso documento = documentoProcessoRepository.findByIdAndProcessoId(idDocumento, idProcesso)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Documento não encontrado"));
        if (documento.getDirecao() != DirecaoDocumento.EMPRESA_ENVIA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este documento não é para aceite");
        }
        if (documento.getStatus() == StatusDocumento.ACEITO || documento.getAceitoEm() != null) { // RN17
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Documento já foi aceito");
        }
        if (documento.getStatus() != StatusDocumento.EM_ANALISE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A empresa ainda não enviou o arquivo");
        }

        Colaborador colaborador = colaboradorRepository.findById(usuario.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado"));

        documento.setAceitoEm(LocalDateTime.now());
        documento.setStatus(StatusDocumento.ACEITO);
        documentoProcessoRepository.save(documento);

        eventoService.registrar(processo, colaborador.getEmail(), AcaoEvento.DOCUMENTO_ACEITO);
        return resposta(documento);
    }

    private DocumentoResponse resposta(DocumentoProcesso d) {
        return new DocumentoResponse(d.getId(), d.getTipoDocumento().getNome(), d.getDescricao(),
                d.getDirecao(), d.getStatus(), null, null, d.getAceitoEm());
    }
}
