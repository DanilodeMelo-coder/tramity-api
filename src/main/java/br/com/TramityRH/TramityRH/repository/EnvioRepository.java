package br.com.TramityRH.TramityRH.repository;

import br.com.TramityRH.TramityRH.model.Envio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnvioRepository extends JpaRepository <Envio, UUID> {
    Optional<Envio> findByIdAndDocumentoProcessoId (UUID id, UUID documentoProcessoId);
}
