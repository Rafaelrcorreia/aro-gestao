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

    // Status que encerram o processo
    public static final java.util.Set<StatusProcesso> FINALIZADOS =
            java.util.Collections.unmodifiableSet(java.util.EnumSet.of(DEFERIDO, INDEFERIDO, ARQUIVADO));

    // Processos finalizados nao contam como "em andamento"
    public boolean isFinalizado() {
        return FINALIZADOS.contains(this);
    }
}
