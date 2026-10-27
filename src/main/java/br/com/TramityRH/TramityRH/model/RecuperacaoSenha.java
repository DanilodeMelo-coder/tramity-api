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

@Getter
@NoArgsConstructor
@Entity
@Table(name = "recuperacao_senha")
public class RecuperacaoSenha {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36)
    private UUID id;

    // Sem chave estrangeira: o e-mail pode ser de um RH ou de um colaborador
    @Setter
    @Column(nullable = false, length = 150)
    private String email;

    @Setter
    @Column(nullable = false, length = 6)
    private String codigo;

    @Setter
    @Column(name = "data_expiracao", nullable = false)
    private LocalDateTime dataExpiracao;

    // Vazio = código ainda não usado (RN18: uso único)
    @Setter
    @Column(name = "usado_em")
    private LocalDateTime usadoEm;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;
}