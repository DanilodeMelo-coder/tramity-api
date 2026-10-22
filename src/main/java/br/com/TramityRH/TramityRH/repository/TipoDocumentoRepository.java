package br.com.TramityRH.TramityRH.repository;
import br.com.TramityRH.TramityRH.model.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, UUID>{ //<TipoDocumento: Tabela que vai acessar, UUID: Tipo de id>

}
