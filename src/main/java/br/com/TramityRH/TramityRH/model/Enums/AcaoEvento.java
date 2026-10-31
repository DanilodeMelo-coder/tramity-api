package br.com.TramityRH.TramityRH.model.Enums;

public enum AcaoEvento {
    DOCUMENTO_ENVIADO("Documento enviado"),
    DOCUMENTO_APROVADO("Documento aprovado"),
    DOCUMENTO_RECUSADO("Documento recusado"),
    PROCESSO_CONCLUIDO("Processo concluido"),
    DOCUMENTO_ACEITO("Documento aceito");

    private final String descricao;

    AcaoEvento(String descricao) {
        this.descricao = descricao;
    }
    public String getDescricao() {
        return descricao;
    }
}
