package br.com.TramityRH.TramityRH.repository;

import br.com.TramityRH.TramityRH.model.UsuarioRhEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import java.util.UUID;

public interface UsuarioRhEmpresaRepository extends JpaRepository <UsuarioRhEmpresa, UUID> {
    Optional<UsuarioRhEmpresa> findByEmail(String email);
    boolean existsByEmail(String email);
}