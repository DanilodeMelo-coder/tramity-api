package br.com.TramityRH.TramityRH.controller;

import br.com.TramityRH.TramityRH.model.TipoDocumento;
import br.com.TramityRH.TramityRH.service.TipoDocumentoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tipos_documentos")
public class TipoDocumentoController {

    private final TipoDocumentoService service;

    public TipoDocumentoController(TipoDocumentoService service) {
        this.service = service;
    }

    @GetMapping
    public List<TipoDocumento> listar(){

        return service.listarTodosDocumentos();
    }
}
