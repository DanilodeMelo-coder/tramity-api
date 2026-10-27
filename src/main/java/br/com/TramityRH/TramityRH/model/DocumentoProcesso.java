package br.com.TramityRH.TramityRH.model;

import br.com.TramityRH.TramityRH.model.Enums.DirecaoDocumento;
import br.com.TramityRH.TramityRH.model.Enums.StatusDocumento;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "documento_processo")
public class DocumentoProcesso {

    @Setter(AccessLevel.NONE) // o id nunca é alterado pelo código
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_processo", nullable = false)
    private Processo processo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_documento", nullable = false)
    private TipoDocumento tipoDocumento;

    // Ex.: "Certidão do filho João" quando o mesmo tipo entra mais de uma vez (RN09)
    @Column(length = 255)
    private String descricao;

    // VARCHAR no banco: sem o JdbcTypeCode, o Hibernate esperaria uma coluna ENUM do MySQL
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private DirecaoDocumento direcao = DirecaoDocumento.COLABORADOR_ENVIA;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private StatusDocumento status = StatusDocumento.PENDENTE;

    // Preenchido quando o colaborador dá o aceite na demissão (RN17)
    @Column(name = "aceito_em")
    private LocalDateTime aceitoEm;
}