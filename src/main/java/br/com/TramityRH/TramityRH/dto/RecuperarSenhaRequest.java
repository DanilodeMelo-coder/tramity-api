package br.com.TramityRH.TramityRH.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RecuperarSenhaRequest(
        @NotBlank @Email
        String email
) {
}
