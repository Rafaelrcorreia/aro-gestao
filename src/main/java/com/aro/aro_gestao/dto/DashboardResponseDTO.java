package com.aro.aro_gestao.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DashboardResponseDTO {

    private LocalDate dataReferencia;
    private long clientesAtivos;
    private long tarefasPendentes;
    private long tarefasAtrasadas;
    private List<TarefaResponseDTO> proximosPrazos = new ArrayList<>();
    private List<AtendimentoResponseDTO> ultimosAtendimentos = new ArrayList<>();

    public DashboardResponseDTO() {
    }

    public LocalDate getDataReferencia() {
        return dataReferencia;
    }

    public void setDataReferencia(LocalDate dataReferencia) {
        this.dataReferencia = dataReferencia;
    }

    public long getClientesAtivos() {
        return clientesAtivos;
    }

    public void setClientesAtivos(long clientesAtivos) {
        this.clientesAtivos = clientesAtivos;
    }

    public long getTarefasPendentes() {
        return tarefasPendentes;
    }

    public void setTarefasPendentes(long tarefasPendentes) {
        this.tarefasPendentes = tarefasPendentes;
    }

    public long getTarefasAtrasadas() {
        return tarefasAtrasadas;
    }

    public void setTarefasAtrasadas(long tarefasAtrasadas) {
        this.tarefasAtrasadas = tarefasAtrasadas;
    }

    public List<TarefaResponseDTO> getProximosPrazos() {
        return proximosPrazos;
    }

    public void setProximosPrazos(List<TarefaResponseDTO> proximosPrazos) {
        this.proximosPrazos = proximosPrazos;
    }

    public List<AtendimentoResponseDTO> getUltimosAtendimentos() {
        return ultimosAtendimentos;
    }

    public void setUltimosAtendimentos(List<AtendimentoResponseDTO> ultimosAtendimentos) {
        this.ultimosAtendimentos = ultimosAtendimentos;
    }
}
