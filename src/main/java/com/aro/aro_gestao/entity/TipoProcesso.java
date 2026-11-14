package com.aro.aro_gestao.entity;

// Tipos de servico/procedimento acompanhados pela consultoria
public enum TipoProcesso {
    GEORREFERENCIAMENTO("Georreferenciamento"),
    CAR("CAR"),
    CCIR("CCIR"),
    ITR("ITR"),
    RETIFICACAO_MATRICULA("Retificação de matrícula"),
    REGULARIZACAO_FUNDIARIA("Regularização fundiária"),
    USUCAPIAO("Usucapião"),
    LICENCIAMENTO_AMBIENTAL("Licenciamento ambiental"),
    OUTORGA_AGUA("Outorga de água"),
    OUTRO("Outro");

    private final String rotulo;

    TipoProcesso(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
