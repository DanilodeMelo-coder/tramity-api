package br.com.TramityRH.TramityRH.dto;

import br.com.TramityRH.TramityRH.model.Enums.TipoProcesso;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CriarProcessoRequest(
        @NotNull
        TipoProcesso tipo,

        @NotEmpty(message = "informe ao menos um documento")
        List<@Valid DocumentoSolicitadoRequest> documentos
) {}
