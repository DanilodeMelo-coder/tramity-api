package br.com.TramityRH.TramityRH.service;

import br.com.TramityRH.TramityRH.model.TipoDocumento;
import br.com.TramityRH.TramityRH.repository.TipoDocumentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TipoDocumentoService {

    private final TipoDocumentoRepository repository;

    public TipoDocumentoService(TipoDocumentoRepository repository) {
        this.repository = repository;
    }

    public List<TipoDocumento> listarTodosDocumentos(){
        return repository.findAll();
    }
}
