package br.com.TramityRH.TramityRH.repository;

import br.com.TramityRH.TramityRH.model.Envio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnvioRepository extends JpaRepository <Envio, UUID> {
    Optional<Envio> findByIdAndDocumentoProcessoId (UUID id, UUID documentoProcessoId);
    Optional<Envio> findFirstByDocumentoProcessoIdOrderByEnviadoEmDesc(UUID idDocumento);
    Optional<Envio> findFirstByDocumentoProcessoIdAndResultadoIsNullOrderByEnviadoEmDesc(UUID idDocumento);
    Optional<Envio> findByIdAndDocumentoProcessoProcessoEmpresaId(UUID id, UUID idEmpresa);
    Optional<Envio> findByIdAndDocumentoProcessoProcessoColaboradorId(UUID id, UUID idColaborador);

    @Query("SELECT e FROM Envio e WHERE e.documentoProcesso.id IN :ids ORDER BY e.enviadoEm DESC")
    List<Envio> listarPorDocumentos(@Param("ids") Collection<UUID> ids);
}
