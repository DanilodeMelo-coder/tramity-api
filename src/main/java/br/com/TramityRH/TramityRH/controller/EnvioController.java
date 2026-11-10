package br.com.TramityRH.TramityRH.controller;

import br.com.TramityRH.TramityRH.dto.EnvioResponse;
import br.com.TramityRH.TramityRH.security.UsuarioLogado;
import br.com.TramityRH.TramityRH.service.EnvioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
public class EnvioController {
    private final EnvioService envioService;

    public EnvioController(EnvioService envioService) {
        this.envioService = envioService;
    }

    @PostMapping(value = "/documentos/{id}/envios", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public EnvioResponse enviar(@PathVariable UUID id,
                                @RequestParam("arquivo") MultipartFile arquivo,
                                @AuthenticationPrincipal Jwt jwt) {
        return envioService.enviar(id, arquivo, UsuarioLogado.de(jwt));
    }
}
