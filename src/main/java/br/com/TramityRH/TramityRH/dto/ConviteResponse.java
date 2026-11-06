package br.com.TramityRH.TramityRH.dto;

import java.time.LocalDateTime;

public record ConviteResponse(
        String codigo, LocalDateTime expiraEm
) {
}
