package br.com.TramityRH.TramityRH.dto;

import java.util.UUID;

public record CadastroResponse(UUID id, String nome, String email) {
}
