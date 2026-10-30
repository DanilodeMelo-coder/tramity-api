package br.com.TramityRH.TramityRH.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public record UsuarioLogado(UUID id, String perfil, UUID idEmpresa) {

    public static UsuarioLogado de(Jwt jwt) {
        String idEmpresa = jwt.getClaimAsString("idEmpresa");
        return new UsuarioLogado(
                UUID.fromString(jwt.getSubject()),
                jwt.getClaimAsString("perfil"),
                idEmpresa != null ? UUID.fromString(idEmpresa) : null
        );
    }

    public boolean isRh() {
        return "RH".equals(perfil);
    }
}
