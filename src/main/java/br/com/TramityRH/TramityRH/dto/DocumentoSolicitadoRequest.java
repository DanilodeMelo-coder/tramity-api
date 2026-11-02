package br.com.TramityRH.TramityRH.dto;

import br.com.TramityRH.TramityRH.model.Enums.DirecaoDocumento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;


public record DocumentoSolicitadoRequest(

        @NotNull
        UUID idTipoDocumento,

        @Size(max = 255)
        String descricao,

        @NotNull
        DirecaoDocumento direcao
) {
}
