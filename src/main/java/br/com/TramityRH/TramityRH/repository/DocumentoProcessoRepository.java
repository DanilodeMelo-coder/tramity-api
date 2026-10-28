package br.com.TramityRH.TramityRH.repository;

import br.com.TramityRH.TramityRH.model.DocumentoProcesso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentoProcessoRepository extends JpaRepository <DocumentoProcesso, UUID> {
    List<DocumentoProcesso> findByProcessoId (UUID processoId);
    Optional<DocumentoProcesso> findByIdAndProcessoId (UUID id, UUID processoId); // um findById "seco" pode devolver o documento de qualquer processo, por isso e melhor buscar tanto pelo id quanto pelo processo
}
