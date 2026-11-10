package br.com.TramityRH.TramityRH.controller;

import br.com.TramityRH.TramityRH.dto.MeuProcessoResponse;
import br.com.TramityRH.TramityRH.security.UsuarioLogado;
import br.com.TramityRH.TramityRH.service.ProcessoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MeusProcessosController {

    private final ProcessoService processoService;

    public MeusProcessosController(ProcessoService processoService) {
        this.processoService = processoService;
    }

    @GetMapping("/meus-processos")
    public List<MeuProcessoResponse> listarMeus(@AuthenticationPrincipal Jwt jwt) {
        return processoService.listarMeus(UsuarioLogado.de(jwt));
    }
}