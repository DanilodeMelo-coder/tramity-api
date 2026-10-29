package br.com.TramityRH.TramityRH.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final long expiracaoHoras;

    public TokenService(JwtEncoder jwtEncoder,
                        @Value("${tramity.jwt.expiracao-horas}") long expiracaoHoras) {
        this.jwtEncoder = jwtEncoder;
        this.expiracaoHoras = expiracaoHoras;
    }

    public String gerarToken(UUID idUsuario, String perfil, UUID idEmpresa) {
        Instant agora = Instant.now();

        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer("tramity-api")
                .issuedAt(agora)
                .expiresAt(agora.plus(expiracaoHoras, ChronoUnit.HOURS))
                .subject(idUsuario.toString())
                .claim("perfil", perfil);

        if (idEmpresa != null) {
            claims.claim("idEmpresa", idEmpresa.toString());
        }

        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(cabecalho, claims.build()))
                .getTokenValue();
    }
}
