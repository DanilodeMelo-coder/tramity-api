package br.com.TramityRH.TramityRH.service;

import br.com.TramityRH.TramityRH.dto.DocumentoResponse;
import br.com.TramityRH.TramityRH.model.DocumentoProcesso;
import br.com.TramityRH.TramityRH.model.Enums.StatusDocumento;
import br.com.TramityRH.TramityRH.model.Envio;
import br.com.TramityRH.TramityRH.repository.EnvioRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class DocumentoMapper {

    private final EnvioRepository envioRepository;

    public DocumentoMapper(EnvioRepository envioRepository) {
        this.envioRepository = envioRepository;
    }

    public List<DocumentoResponse> montar(List<DocumentoProcesso> documentos, boolean exibirAvaliador) {
        if (documentos.isEmpty()) {
            return List.of();
        }

        List<UUID> ids = documentos.stream().map(DocumentoProcesso::getId).toList();

        Map<UUID, Envio> ultimos = new HashMap<>();
        for (Envio e : envioRepository.listarPorDocumentos(ids)) {
            ultimos.putIfAbsent(e.getDocumentoProcesso().getId(), e);
        }

        return documentos.stream().map(d -> {
            Envio ultimo = ultimos.get(d.getId());
            UUID idUltimoEnvio = ultimo != null ? ultimo.getId() : null;
            String motivo = (ultimo != null && d.getStatus() == StatusDocumento.RECUSADO)
                    ? ultimo.getMotivoRecusa() : null;

            String avaliadoPor = null;
            LocalDateTime dataAvaliacao = null;
            if (exibirAvaliador && ultimo != null && ultimo.getAvaliadoPor() != null) {
                avaliadoPor = ultimo.getAvaliadoPor().getNome();
                dataAvaliacao = ultimo.getDataAvaliacao();
            }

            return new DocumentoResponse(d.getId(), d.getTipoDocumento().getNome(), d.getDescricao(),
                    d.getDirecao(), d.getStatus(), motivo, idUltimoEnvio, d.getAceitoEm(),
                    avaliadoPor, dataAvaliacao);
        }).toList();
    }

    public DocumentoResponse montar(DocumentoProcesso documento, boolean exibirAvaliador) {
        return montar(List.of(documento), exibirAvaliador).get(0);
    }
}