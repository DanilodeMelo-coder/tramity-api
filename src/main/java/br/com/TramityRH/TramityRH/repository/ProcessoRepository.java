package br.com.TramityRH.TramityRH.repository;

import br.com.TramityRH.TramityRH.model.Processo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcessoRepository extends JpaRepository <Processo, UUID> {
    List<Processo> findByEmpresaId (UUID empresaId); // ← lista do rh, processos da empresa dele
    Optional<Processo> findByIdAndEmpresaId (UUID id, UUID empresaId); // ← essa bosta garante que a busca seja de um processo da empresa do usuario logado
}
