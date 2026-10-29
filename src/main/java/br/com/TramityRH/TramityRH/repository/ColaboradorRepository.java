package br.com.TramityRH.TramityRH.repository;

import br.com.TramityRH.TramityRH.model.Colaborador;
import br.com.TramityRH.TramityRH.model.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ColaboradorRepository extends JpaRepository <Colaborador, UUID>{
    Optional<ColaboradorRepository> findByEmail(String email);
}
