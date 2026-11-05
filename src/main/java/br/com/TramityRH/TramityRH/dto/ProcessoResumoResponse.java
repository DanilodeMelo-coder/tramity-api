package br.com.TramityRH.TramityRH.dto;

import br.com.TramityRH.TramityRH.model.Enums.TipoProcesso;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProcessoResumoResponse(
        UUID id,
        TipoProcesso tipo,
        String nomeColaborador,
        String emailConvite,
        int aprovados,
        int total,
        int aguardandoAnalise,
        LocalDateTime dataCriacao,
        LocalDateTime dataConclusao
) {
}
