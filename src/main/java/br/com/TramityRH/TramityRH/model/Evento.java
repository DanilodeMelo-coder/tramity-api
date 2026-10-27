package br.com.TramityRH.TramityRH.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

// Histórico para auditoria (RNF08). Um evento nunca é alterado depois de gravado.
@Getter
@NoArgsConstructor
@Entity
@Table(name = "evento")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36)
    private UUID id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_processo", nullable = false)
    private Processo processo;

    // Texto com o nome de quem fez a ação
    @Setter
    @Column(nullable = false, length = 150)
    private String autor;

    @Setter
    @Column(nullable = false, length = 255)
    private String acao;

    // Gerada na API
    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;
}