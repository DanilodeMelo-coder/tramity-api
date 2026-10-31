package br.com.TramityRH.TramityRH.repository;

import br.com.TramityRH.TramityRH.model.Colaborador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ColaboradorRepository extends JpaRepository <Colaborador, UUID>{
    Optional<Colaborador> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
}
