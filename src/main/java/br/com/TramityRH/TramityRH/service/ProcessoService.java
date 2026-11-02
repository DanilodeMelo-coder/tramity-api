package br.com.TramityRH.TramityRH.service;

import br.com.TramityRH.TramityRH.dto.CriarProcessoRequest;
import br.com.TramityRH.TramityRH.dto.DocumentoResponse;
import br.com.TramityRH.TramityRH.dto.DocumentoSolicitadoRequest;
import br.com.TramityRH.TramityRH.dto.ProcessoDetalheResponse;
import br.com.TramityRH.TramityRH.model.DocumentoProcesso;
import br.com.TramityRH.TramityRH.model.Enums.DirecaoDocumento;
import br.com.TramityRH.TramityRH.model.Enums.StatusDocumento;
import br.com.TramityRH.TramityRH.model.Enums.TipoProcesso;
import br.com.TramityRH.TramityRH.model.Processo;
import br.com.TramityRH.TramityRH.model.TipoDocumento;
import br.com.TramityRH.TramityRH.model.UsuarioRhEmpresa;
import br.com.TramityRH.TramityRH.repository.DocumentoProcessoRepository;
import br.com.TramityRH.TramityRH.repository.ProcessoRepository;
import br.com.TramityRH.TramityRH.repository.TipoDocumentoRepository;
import br.com.TramityRH.TramityRH.repository.UsuarioRhEmpresaRepository;
import br.com.TramityRH.TramityRH.security.UsuarioLogado;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProcessoService {
    private final ProcessoRepository processoRepository;
    private final DocumentoProcessoRepository documentoRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final UsuarioRhEmpresaRepository usuarioRhRepository;

    public ProcessoService(ProcessoRepository processoRepository,
                           DocumentoProcessoRepository documentoRepository,
                           TipoDocumentoRepository tipoDocumentoRepository,
                           UsuarioRhEmpresaRepository usuarioRhRepository) {
        this.processoRepository = processoRepository;
        this.documentoRepository = documentoRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.usuarioRhRepository = usuarioRhRepository;
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

    private ProcessoDetalheResponse montarDetalhe(Processo processo, List<DocumentoProcesso> documentos) {
        List<DocumentoResponse> itens = documentos.stream()
                .map(d -> new DocumentoResponse(
                        d.getId(),
                        d.getTipoDocumento().getNome(),
                        d.getDescricao(),
                        d.getDirecao(),
                        d.getStatus(),
                        null,
                        null,
                        d.getAceitoEm()))
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

