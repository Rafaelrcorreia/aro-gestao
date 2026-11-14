package com.aro.aro_gestao.dto;

import com.aro.aro_gestao.entity.Prioridade;
import com.aro.aro_gestao.entity.StatusTarefa;
import com.aro.aro_gestao.entity.Tarefa;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class TarefaResponseDTO {

    private Long id;
    private Long clienteId;
    private String nomeCliente;
    private String titulo;
    private String descricao;
    private String responsavel;
    private Prioridade prioridade;
    private LocalDate prazo;
    private StatusTarefa status;
    private boolean atrasada;
    private List<ItemChecklistResponseDTO> itensChecklist;

    public TarefaResponseDTO() {
    }

    public TarefaResponseDTO(Tarefa tarefa) {
        this.id = tarefa.getId();
        this.clienteId = tarefa.getCliente().getId();
        this.nomeCliente = tarefa.getCliente().getNomeCompleto();
        this.titulo = tarefa.getTitulo();
        this.descricao = tarefa.getDescricao();
        this.responsavel = tarefa.getResponsavel();
        this.prioridade = tarefa.getPrioridade();
        this.prazo = tarefa.getPrazo();
        this.status = tarefa.getStatus();
        this.atrasada = tarefa.getStatus() != StatusTarefa.CONCLUIDA
                && tarefa.getPrazo() != null
                && tarefa.getPrazo().isBefore(LocalDate.now());
        this.itensChecklist = tarefa.getItens()
                .stream()
                .map(ItemChecklistResponseDTO::new)
                .collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(Prioridade prioridade) {
        this.prioridade = prioridade;
    }

    public LocalDate getPrazo() {
        return prazo;
    }

    public void setPrazo(LocalDate prazo) {
        this.prazo = prazo;
    }

    public StatusTarefa getStatus() {
        return status;
    }

    public void setStatus(StatusTarefa status) {
        this.status = status;
    }

    public boolean isAtrasada() {
        return atrasada;
    }

    public void setAtrasada(boolean atrasada) {
        this.atrasada = atrasada;
    }

    public List<ItemChecklistResponseDTO> getItensChecklist() {
        return itensChecklist;
    }

    public void setItensChecklist(List<ItemChecklistResponseDTO> itensChecklist) {
        this.itensChecklist = itensChecklist;
    }
}