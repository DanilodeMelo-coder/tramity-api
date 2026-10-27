package br.com.TramityRH.TramityRH.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "convite")

public class Convite {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) // esse fetch lazy adia a consulta ao banco, pra melhor desempenho
    @JoinColumn(name = "id_processo", nullable = false)
    private Processo processo;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "codigo", nullable = false, length = 6)
    private String codigo;

    @Column(name = "data_expiracao", nullable = false)
    private LocalDateTime dataExpiracao;

    @Column(name = "usado_em", nullable = false)
    private LocalDateTime usadoEm;

}
