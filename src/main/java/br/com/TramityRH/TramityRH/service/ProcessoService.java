package br.com.TramityRH.TramityRH.service;

import br.com.TramityRH.TramityRH.dto.*;
import br.com.TramityRH.TramityRH.model.*;
import br.com.TramityRH.TramityRH.model.Enums.DirecaoDocumento;
import br.com.TramityRH.TramityRH.model.Enums.StatusDocumento;
import br.com.TramityRH.TramityRH.model.Enums.TipoProcesso;
import br.com.TramityRH.TramityRH.repository.*;
import br.com.TramityRH.TramityRH.security.UsuarioLogado;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProcessoService {
    private final ProcessoRepository processoRepository;
    private final DocumentoProcessoRepository documentoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final UsuarioRhEmpresaRepository usuarioRhRepository;
    private final EnvioRepository envioRepository;
    private final ConviteRepository conviteRepository;

    public ProcessoService(ProcessoRepository processoRepository,
                           DocumentoProcessoRepository documentoRepository,
                           TipoDocumentoRepository tipoDocumentoRepository,
                           UsuarioRhEmpresaRepository usuarioRhRepository,
                           EnvioRepository envioRepository,
                           ConviteRepository conviteRepository) {
        this.processoRepository = processoRepository;
        this.documentoRepository = documentoRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.usuarioRhRepository = usuarioRhRepository;
        this.envioRepository = envioRepository;
        this.conviteRepository = conviteRepository;
    }

    @Transactional
    public ProcessoDetalheResponse criar(CriarProcessoRequest dados, UsuarioLogado usuario) {
        // 1. Só o RH
        if (!usuario.isRh()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas o RH pode abrir processos");
        }

        // 2. Admissão não tem documento enviado pela empresa
        for (DocumentoSolicitadoRequest doc : dados.documentos()) {
            if (dados.tipo() == TipoProcesso.ADMISSAO && doc.direcao() == DirecaoDocumento.EMPRESA_ENVIA) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Na admissão, todos os documentos são enviados pelo colaborador");
            }
        }


        // 3. O RH logado, para saber a empresa
        UsuarioRhEmpresa rh = usuarioRhRepository.findById(usuario.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado"));

        // 4. O processo
        Processo processo = new Processo();
        processo.setEmpresa(rh.getEmpresa());
        processo.setCriadoPor(rh);
        processo.setTipo(dados.tipo());
        processoRepository.save(processo);

        // 5. Os documentos pedidos
        List<DocumentoProcesso> documentos = new ArrayList<>();
        for (DocumentoSolicitadoRequest doc : dados.documentos()) {
            TipoDocumento tipo = tipoDocumentoRepository.findById(doc.idTipoDocumento())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Tipo de documento não encontrado"));

            DocumentoProcesso documento = new DocumentoProcesso();
            documento.setProcesso(processo);
            documento.setTipoDocumento(tipo);
            documento.setDescricao(doc.descricao());
            documento.setDirecao(doc.direcao());
            documentos.add(documentoRepository.save(documento));
        }

        // AUDITORIA: PROCESSO_CRIADO

        // 6. A resposta
        return montarDetalhe(processo, documentos);
    }

    @Transactional(readOnly = true)
    public ProcessoDetalheResponse detalhar(UUID id, UsuarioLogado usuario) {
        Processo processo = processoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Processo não encontrado"));

        verificarAcesso(processo, usuario);

        List<DocumentoProcesso> documentos = documentoRepository.findByProcessoId(id);
        return montarDetalhe(processo, documentos);
    }

    private void verificarAcesso(Processo processo, UsuarioLogado usuario) {
        boolean permitido;
        if (usuario.isRh()) {
            permitido = processo.getEmpresa().getId().equals(usuario.idEmpresa());
        } else {
            permitido = processo.getColaborador() != null
                    && processo.getColaborador().getId().equals(usuario.id());
        }

        if (!permitido) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sem acesso a este processo");
        }
    }

    @Transactional(readOnly = true)
    public List<ProcessoResumoResponse> listar(String status, UsuarioLogado usuario) {
        if (!usuario.isRh()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas o RH pode listar processos");
        }

        List<Processo> processos = processoRepository.findByEmpresaIdOrderByDataCriacaoDesc(usuario.idEmpresa());

        return processos.stream()
                .filter(p -> status == null || status.equals(statusDe(p)))
                .map(p -> montarResumo(p))
                .toList();
    }

    private String statusDe(Processo processo) {
        return (processo.getDataConclusao() == null) ? "EM_ANDAMENTO" : "CONCLUIDO";
    }

    private ProcessoResumoResponse montarResumo(Processo processo) {
        List<DocumentoProcesso> documentos = documentoRepository.findByProcessoId(processo.getId());

        int aprovados = (int) documentos.stream()
                .filter(d -> d.getStatus() == StatusDocumento.APROVADO || d.getStatus() == StatusDocumento.ACEITO)
                .count();

        int aguardando = (int) documentos.stream()
                .filter(d -> d.getStatus() == StatusDocumento.EM_ANALISE)
                .count();

        String nomeColaborador = (processo.getColaborador() == null) ? null : processo.getColaborador().getNome();

        String emailConvite = conviteRepository.findFirstByProcessoIdOrderByDataExpiracaoDesc(processo.getId())
                .map(c -> c.getEmail())
                .orElse(null);

        return new ProcessoResumoResponse(processo.getId(), processo.getTipo(), nomeColaborador, emailConvite,
                aprovados, documentos.size(), aguardando, processo.getDataCriacao(), processo.getDataConclusao());
    }

    private ProcessoDetalheResponse montarDetalhe(Processo processo, List<DocumentoProcesso> documentos) {
        List<DocumentoResponse> itens = documentos.stream()
                .map(d -> {
                    Optional<Envio> ultimo = envioRepository.findFirstByDocumentoProcessoIdOrderByEnviadoEmDesc(d.getId());
                    return new DocumentoResponse(
                            d.getId(),
                            d.getTipoDocumento().getNome(),
                            d.getDescricao(),
                            d.getDirecao(),
                            d.getStatus(),
                            ultimo.map(e -> e.getMotivoRecusa()).orElse(null),
                            ultimo.map(e -> e.getId()).orElse(null),
                            d.getAceitoEm());
                })
                .toList();

        int aprovados = (int) documentos.stream()
                .filter(d -> d.getStatus() == StatusDocumento.APROVADO || d.getStatus() == StatusDocumento.ACEITO)
                .count();

        String status = (processo.getDataConclusao() == null) ? "EM_ANDAMENTO" : "CONCLUIDO";
        String colaborador = (processo.getColaborador() == null) ? null : processo.getColaborador().getNome();

        return new ProcessoDetalheResponse(processo.getId(), processo.getTipo(), status,
                colaborador, aprovados, documentos.size(), itens);
    }
}

