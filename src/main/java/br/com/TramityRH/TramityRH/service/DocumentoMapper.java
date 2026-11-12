package br.com.TramityRH.TramityRH.service;

import br.com.TramityRH.TramityRH.dto.DocumentoResponse;
import br.com.TramityRH.TramityRH.model.DocumentoProcesso;
import br.com.TramityRH.TramityRH.model.Enums.StatusDocumento;
import br.com.TramityRH.TramityRH.model.Envio;
import br.com.TramityRH.TramityRH.repository.EnvioRepository;
import org.springframework.stereotype.Component;

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

    public List<DocumentoResponse> montar(List<DocumentoProcesso> documentos) {
        if (documentos.isEmpty()) {
            return List.of();
        }

        List<UUID> ids = documentos.stream().map(DocumentoProcesso::getId).toList();

        // a lista vem do envio mais recente para o mais antigo: o primeiro de cada documento é o último
        Map<UUID, Envio> ultimos = new HashMap<>();
        for (Envio e : envioRepository.listarPorDocumentos(ids)) {
            ultimos.putIfAbsent(e.getDocumentoProcesso().getId(), e);
        }

        return documentos.stream().map(d -> {
            Envio ultimo = ultimos.get(d.getId());
            UUID idUltimoEnvio = ultimo != null ? ultimo.getId() : null;
            String motivo = (ultimo != null && d.getStatus() == StatusDocumento.RECUSADO)
                    ? ultimo.getMotivoRecusa() : null;
            return new DocumentoResponse(d.getId(), d.getTipoDocumento().getNome(), d.getDescricao(),
                    d.getDirecao(), d.getStatus(), motivo, idUltimoEnvio, d.getAceitoEm());
        }).toList();
    }

    public DocumentoResponse montar(DocumentoProcesso documento) {
        return montar(List.of(documento)).get(0);
    }
}