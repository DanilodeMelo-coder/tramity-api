package br.com.TramityRH.TramityRH.service;

import br.com.TramityRH.TramityRH.model.Enums.AcaoEvento;
import br.com.TramityRH.TramityRH.model.Evento;
import br.com.TramityRH.TramityRH.model.Processo;
import br.com.TramityRH.TramityRH.repository.EventoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service

public class EventoService {
    private final EventoRepository eventoRepository;

    public EventoService(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrar(Processo processo, String autor, AcaoEvento acao) {
        Objects.requireNonNull(processo, "processo");
        Objects.requireNonNull(acao, "acao");
        Objects.requireNonNull(autor, "autor");

        Evento evento = new Evento();
        evento.setProcesso(processo);
        evento.setAutor(cortar(autor, 150));
        evento.setAcao(cortar(acao.getDescricao(), 255));
        eventoRepository.save(evento);
    }

    private String cortar(String texto, int max) {
        return texto.length() <= max ? texto : texto.substring(0, max);
    }
}
