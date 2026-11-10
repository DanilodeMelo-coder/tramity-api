package br.com.TramityRH.TramityRH.repository;

import br.com.TramityRH.TramityRH.model.DocumentoProcesso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentoProcessoRepository extends JpaRepository <DocumentoProcesso, UUID> {
    List<DocumentoProcesso> findByProcessoId (UUID processoId);
    Optional<DocumentoProcesso> findByIdAndProcessoId (UUID id, UUID processoId); // um findById "seco" pode devolver o documento de qualquer processo, por isso e melhor buscar tanto pelo id quanto pelo processo
    Optional<DocumentoProcesso> findByIdAndProcessoColaboradorId(UUID id, UUID idColaborador);

    @Query("SELECT d from DocumentoProcesso d JOIN FETCH d.tipoDocumento WHERE d.processo.id = :processoId")
    List<DocumentoProcesso> listarComTipo (@Param("processoId") UUID processoId);

    @Query("SELECT d FROM DocumentoProcesso d WHERE d.processo.colaborador.id = :colaboradorId")
    List<DocumentoProcesso> listarPorColaborador(@Param("colaboradorId") UUID colaboradorId);
}
