package br.com.TramityRH.TramityRH.repository;

import br.com.TramityRH.TramityRH.model.RecuperacaoSenha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RecuperacaoSenhaRepository extends JpaRepository <RecuperacaoSenha, UUID> {
    Optional<RecuperacaoSenha> findByEmailAndCodigoOrderByDataCriacaoDesc (String email, String codigo); // ← validar os dois juntos por seguranca!!
}
