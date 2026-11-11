package br.com.TramityRH.TramityRH.controller;

import br.com.TramityRH.TramityRH.dto.ConcluirProcessoRequest;
import br.com.TramityRH.TramityRH.dto.CriarProcessoRequest;
import br.com.TramityRH.TramityRH.dto.ProcessoDetalheResponse;
import br.com.TramityRH.TramityRH.dto.ProcessoResumoResponse;
import br.com.TramityRH.TramityRH.security.UsuarioLogado;
import br.com.TramityRH.TramityRH.service.ProcessoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @GetMapping("/{id}")
    public ProcessoDetalheResponse detalhar(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return processoService.detalhar(id, UsuarioLogado.de(jwt));
    }

    @GetMapping
    public List<ProcessoResumoResponse> listar(@RequestParam(required = false) String status,
                                               @AuthenticationPrincipal Jwt jwt) {
        return processoService.listar(status, UsuarioLogado.de(jwt));
    }

    @PostMapping("/{id}/concluir")
    public ProcessoDetalheResponse concluir(@PathVariable UUID id,
                                            @RequestBody(required = false) ConcluirProcessoRequest dados,
                                            @AuthenticationPrincipal Jwt jwt) {
        boolean confirmar = dados != null && dados.confirmarPendencias();
        return processoService.concluir(id, confirmar, UsuarioLogado.de(jwt));
    }
}
