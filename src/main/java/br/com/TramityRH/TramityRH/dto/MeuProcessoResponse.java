package br.com.TramityRH.TramityRH.dto;

import br.com.TramityRH.TramityRH.model.Enums.TipoProcesso;

import java.util.UUID;

public record MeuProcessoResponse(
        UUID id, TipoProcesso tipo, String status, String empresa,
        int aprovados, int total, int pendentes) {}
