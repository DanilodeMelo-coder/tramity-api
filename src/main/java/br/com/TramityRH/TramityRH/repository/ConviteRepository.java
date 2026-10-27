package br.com.TramityRH.TramityRH.repository;

import br.com.TramityRH.TramityRH.model.Convite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConviteRepository extends JpaRepository <Convite, UUID> {
    Optional<Convite> findByCodigo (String codigo);
}
