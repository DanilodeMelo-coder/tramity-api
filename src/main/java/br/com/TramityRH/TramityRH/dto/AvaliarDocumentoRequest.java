package br.com.TramityRH.TramityRH.dto;

import br.com.TramityRH.TramityRH.model.Enums.ResultadoAvaliacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AvaliarDocumentoRequest(
        @NotNull ResultadoAvaliacao resultado,
        @Size(max = 500) String motivo) {}