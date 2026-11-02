package br.com.TramityRH.TramityRH.dto;

import br.com.TramityRH.TramityRH.model.Enums.TipoProcesso;

import java.util.List;
import java.util.UUID;

public record ProcessoDetalheResponse(
        UUID id,
        TipoProcesso tipo,
        String status,
        String colaborador,
        int aprovados,
        int total,
        List<DocumentoResponse> documentos
) {
}
