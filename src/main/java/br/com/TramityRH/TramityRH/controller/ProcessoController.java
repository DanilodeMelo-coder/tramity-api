package br.com.TramityRH.TramityRH.controller;

import br.com.TramityRH.TramityRH.dto.CriarProcessoRequest;
import br.com.TramityRH.TramityRH.dto.ProcessoDetalheResponse;
import br.com.TramityRH.TramityRH.security.UsuarioLogado;
import br.com.TramityRH.TramityRH.service.ProcessoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/processos")
public class ProcessoController {

    private final ProcessoService processoService;

    public ProcessoController(ProcessoService processoService) {
        this.processoService = processoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProcessoDetalheResponse criar(@RequestBody @Valid CriarProcessoRequest dados,
                                         @AuthenticationPrincipal Jwt jwt) {
        return processoService.criar(dados, UsuarioLogado.de(jwt));
    }
}
