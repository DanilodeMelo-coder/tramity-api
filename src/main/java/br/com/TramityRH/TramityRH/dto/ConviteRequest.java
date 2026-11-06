package br.com.TramityRH.TramityRH.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConviteRequest(
        @NotBlank @Email @Size(max = 150)
        String email
) {
}
