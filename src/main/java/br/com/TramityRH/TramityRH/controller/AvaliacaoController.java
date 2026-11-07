package br.com.TramityRH.TramityRH.controller;

import br.com.TramityRH.TramityRH.dto.AvaliarDocumentoRequest;
import br.com.TramityRH.TramityRH.dto.DocumentoResponse;
import br.com.TramityRH.TramityRH.security.UsuarioLogado;
import br.com.TramityRH.TramityRH.service.AvaliacaoService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/processos")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @PutMapping("/{idProcesso}/documentos/{idDocumento}/avaliacao")
    public DocumentoResponse avaliar(@PathVariable UUID idProcesso,
                                     @PathVariable UUID idDocumento,
                                     @RequestBody @Valid AvaliarDocumentoRequest dados,
                                     @AuthenticationPrincipal Jwt jwt) {
        return avaliacaoService.avaliar(idProcesso, idDocumento, dados, UsuarioLogado.de(jwt));
    }
}