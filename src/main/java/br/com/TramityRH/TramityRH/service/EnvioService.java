package br.com.TramityRH.TramityRH.service;

import br.com.TramityRH.TramityRH.dto.EnvioResponse;
import br.com.TramityRH.TramityRH.model.DocumentoProcesso;
import br.com.TramityRH.TramityRH.model.Enums.DirecaoDocumento;
import br.com.TramityRH.TramityRH.model.Enums.StatusDocumento;
import br.com.TramityRH.TramityRH.model.Enums.AcaoEvento;
import br.com.TramityRH.TramityRH.model.Envio;
import br.com.TramityRH.TramityRH.model.Processo;
import br.com.TramityRH.TramityRH.repository.DocumentoProcessoRepository;
import br.com.TramityRH.TramityRH.repository.EnvioRepository;
import br.com.TramityRH.TramityRH.security.UsuarioLogado;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
public class EnvioService {

    private final DocumentoProcessoRepository documentoRepository;
    private final EnvioRepository envioRepository;
    private final ArmazenamentoService armazenamentoService;
    private final EventoService eventoService;

    public EnvioService(DocumentoProcessoRepository documentoRepository,
                        EnvioRepository envioRepository,
                        ArmazenamentoService armazenamentoService,
                        EventoService eventoService) {
        this.documentoRepository = documentoRepository;
        this.envioRepository = envioRepository;
        this.armazenamentoService = armazenamentoService;
        this.eventoService = eventoService;
    }

    @Transactional
    public EnvioResponse enviar(UUID idDocumento, MultipartFile arquivo, UsuarioLogado usuario) {
        // 1. Só o colaborador
        if (usuario.isRh()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas o colaborador envia documentos por esta rota");
        }

        // 2. O documento, se for de um processo deste colaborador
        DocumentoProcesso documento = documentoRepository.findByIdAndProcessoColaboradorId(idDocumento, usuario.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Documento não encontrado"));

        Processo processo = documento.getProcesso();

        // 3. Processo concluído não aceita envios (RN16)
        if (processo.getDataConclusao() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este processo já foi concluído");
        }

        // 4. Só documentos que o colaborador envia
        if (documento.getDirecao() != DirecaoDocumento.COLABORADOR_ENVIA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este documento é enviado pela empresa");
        }

        // 5. Só pendente ou recusado (RN12)
        if (documento.getStatus() != StatusDocumento.PENDENTE
                && documento.getStatus() != StatusDocumento.RECUSADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este documento não está aguardando envio");
        }

        // 6. Salva o arquivo
        String caminho = armazenamentoService.salvar(arquivo, processo, documento.getTipoDocumento().getNome());

        // 7. Novo envio e documento em análise (RN10, RN13)
        Envio envio = new Envio();
        envio.setDocumentoProcesso(documento);
        envio.setArquivoUrl(caminho);
        envioRepository.save(envio);

        documento.setStatus(StatusDocumento.EM_ANALISE);
        documentoRepository.save(documento);

        eventoService.registrar(processo, processo.getColaborador().getEmail(), AcaoEvento.DOCUMENTO_ENVIADO); // auditoria

        return new EnvioResponse(envio.getId(), documento.getStatus());
    }

    @Transactional(readOnly = true)
    public Resource baixar(UUID idEnvio, UsuarioLogado usuario) {
        Optional<Envio> encontrado = usuario.isRh()
                ? envioRepository.findByIdAndDocumentoProcessoProcessoEmpresaId(idEnvio, usuario.idEmpresa())
                : envioRepository.findByIdAndDocumentoProcessoProcessoColaboradorId(idEnvio, usuario.id());

        Envio envio = encontrado
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Arquivo não encontrado"));

        return armazenamentoService.carregar(envio.getArquivoUrl());
    }
}