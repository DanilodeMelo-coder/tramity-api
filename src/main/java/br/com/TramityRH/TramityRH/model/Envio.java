package br.com.TramityRH.TramityRH.model;

import br.com.TramityRH.TramityRH.model.Enums.ResultadoAvaliacao;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

// Cada tentativa de envio fica guardada; um reenvio cria um Envio novo (RN13)
@Getter
@NoArgsConstructor
@Entity
@Table(name = "envio")
public class Envio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36)
    private UUID id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_documento_processo", nullable = false)
    private DocumentoProcesso documentoProcesso;

    // Caminho do arquivo dentro da pasta da empresa/colaborador
    @Setter
    @Column(name = "arquivo_url", nullable = false, length = 500)
    private String arquivoUrl;

    @CreationTimestamp
    @Column(name = "enviado_em", nullable = false, updatable = false)
    private LocalDateTime enviadoEm;

    // Os quatro campos abaixo ficam vazios até o RH avaliar
    @Setter
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 20)
    private ResultadoAvaliacao resultado;

    @Setter
    @Column(name = "motivo_recusa", length = 500)
    private String motivoRecusa;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avaliado_por")
    private UsuarioRhEmpresa avaliadoPor;

    @Setter
    @Column(name = "data_avaliacao")
    private LocalDateTime dataAvaliacao;
}
