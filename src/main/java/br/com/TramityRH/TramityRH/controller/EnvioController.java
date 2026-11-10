package br.com.TramityRH.TramityRH.controller;

import br.com.TramityRH.TramityRH.dto.EnvioResponse;
import br.com.TramityRH.TramityRH.security.UsuarioLogado;
import br.com.TramityRH.TramityRH.service.EnvioService;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
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

    @GetMapping("/envios/{id}/arquivo")
    public ResponseEntity<Resource> baixar(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        Resource arquivo = envioService.baixar(id, UsuarioLogado.de(jwt));

        MediaType tipo = MediaTypeFactory.getMediaType(arquivo)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + arquivo.getFilename() + "\"")
                .body(arquivo);
    }
}
