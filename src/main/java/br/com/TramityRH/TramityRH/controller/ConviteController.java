package br.com.TramityRH.TramityRH.controller;

import br.com.TramityRH.TramityRH.dto.AceitarConviteRequest;
import br.com.TramityRH.TramityRH.dto.AceitarConviteResponse;
import br.com.TramityRH.TramityRH.dto.ConviteRequest;
import br.com.TramityRH.TramityRH.dto.ConviteResponse;
import br.com.TramityRH.TramityRH.security.UsuarioLogado;
import br.com.TramityRH.TramityRH.service.ConviteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class ConviteController {

    private final ConviteService conviteService;

    public ConviteController(ConviteService conviteService) {
        this.conviteService = conviteService;
    }

    @PostMapping("/processos/{id}/convites")
    @ResponseStatus(HttpStatus.CREATED)
    public ConviteResponse criar(@PathVariable UUID id,
                                 @RequestBody @Valid ConviteRequest dados,
                                 @AuthenticationPrincipal Jwt jwt) {
        return conviteService.criar(id, dados, UsuarioLogado.de(jwt));
    }

    @PostMapping("/convites/aceitar")
    public AceitarConviteResponse aceitar(@RequestBody @Valid AceitarConviteRequest dados,
                                          @AuthenticationPrincipal Jwt jwt) {
        return conviteService.aceitar(dados, UsuarioLogado.de(jwt));
    }
}
