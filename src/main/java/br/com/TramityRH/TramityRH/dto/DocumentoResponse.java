package br.com.TramityRH.TramityRH.dto;

import br.com.TramityRH.TramityRH.model.Enums.DirecaoDocumento;
import br.com.TramityRH.TramityRH.model.Enums.StatusDocumento;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentoResponse(
        UUID id,
        String nomeTipo,
        String descricao,
        DirecaoDocumento direcao,
        StatusDocumento status,
        String motivoRecusa,
        UUID idUltimoEnvio,
        LocalDateTime aceitoEm
) {
}
