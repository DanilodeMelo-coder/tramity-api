package br.com.TramityRH.TramityRH.repository;

import br.com.TramityRH.TramityRH.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventoRepository extends JpaRepository <Evento, UUID> {
    List<Evento> findByProcessoIdOrderByDataCriacaoAsc (UUID idProcesso);
}
