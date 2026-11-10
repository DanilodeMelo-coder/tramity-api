package br.com.TramityRH.TramityRH.controller;

import br.com.TramityRH.TramityRH.dto.DocumentoResponse;
import br.com.TramityRH.TramityRH.security.UsuarioLogado;
import br.com.TramityRH.TramityRH.service.DemissaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/processos")
public class DemissaoController {

    private final DemissaoService demissaoService;

    public DemissaoController(DemissaoService demissaoService) {
        this.demissaoService = demissaoService;
    }

    @PostMapping(value = "/{idProcesso}/documentos/{idDocumento}/arquivo-empresa",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentoResponse enviarArquivo(@PathVariable UUID idProcesso,
                                           @PathVariable UUID idDocumento,
                                           @RequestParam("arquivo") MultipartFile arquivo,
                                           @AuthenticationPrincipal Jwt jwt) {
        return demissaoService.enviarArquivo(idProcesso, idDocumento, arquivo, UsuarioLogado.de(jwt));
    }

    @PostMapping("/{idProcesso}/documentos/{idDocumento}/aceite")
    public DocumentoResponse aceitar(@PathVariable UUID idProcesso,
                                     @PathVariable UUID idDocumento,
                                     @AuthenticationPrincipal Jwt jwt) {
        return demissaoService.aceitar(idProcesso, idDocumento, UsuarioLogado.de(jwt));
    }
}
