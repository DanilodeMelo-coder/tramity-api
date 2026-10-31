package br.com.TramityRH.TramityRH.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CadastroRequest(

        @NotBlank @Size(max = 150)
        String nome,

        @NotBlank @Pattern(regexp = "\\d{11}", message = "Cpf deve ter 11 dígitos, apenas números")
        String cpf,

        @NotBlank @Email @Size(max = 150)
        String email,

        @NotBlank @Size(min = 8 , max = 72)
        String senha
) {
}
