package br.com.TramityRH.TramityRH.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record NovaSenhaRequest(
        @NotNull
        UUID tokenRedefinicao,

        @NotBlank @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
        String novaSenha
) {
}
