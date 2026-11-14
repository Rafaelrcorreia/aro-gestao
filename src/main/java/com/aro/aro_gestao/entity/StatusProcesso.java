package com.aro.aro_gestao.entity;

public enum StatusProcesso {
    // Reunindo documentos, ainda nao enviado ao orgao
    EM_ELABORACAO,
    PROTOCOLADO,
    EM_ANALISE,
    // O orgao pediu complementacao (pendencia a resolver)
    EXIGENCIA,
    DEFERIDO,
    INDEFERIDO,
    ARQUIVADO;

    // Processos finalizados nao contam como "em andamento"
    public boolean isFinalizado() {
        return this == DEFERIDO || this == INDEFERIDO || this == ARQUIVADO;
    }
}
