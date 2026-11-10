package br.com.TramityRH.TramityRH.dto;

import br.com.TramityRH.TramityRH.model.Enums.StatusDocumento;

import java.util.UUID;

public record EnvioResponse(UUID idEnvio, StatusDocumento status) {
}
