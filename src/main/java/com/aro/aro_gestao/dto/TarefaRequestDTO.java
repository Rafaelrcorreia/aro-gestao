package com.aro.aro_gestao.dto;

import com.aro.aro_gestao.entity.Prioridade;
import com.aro.aro_gestao.entity.StatusTarefa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class TarefaRequestDTO {

    @NotNull(message = "O cliente e obrigatorio")
    private Long clienteId;

    @NotBlank(message = "O titulo e obrigatorio")
    @Size(max = 150, message = "O titulo deve ter no maximo 150 caracteres")
    private String titulo;

    @Size(max = 2000, message = "A descricao deve ter no maximo 2000 caracteres")
    private String descricao;

    @NotBlank(message = "O responsavel e obrigatorio")
    @Size(max = 100, message = "O responsavel deve ter no maximo 100 caracteres")
    private String responsavel;

    @NotNull(message = "A prioridade e obrigatoria")
    private Prioridade prioridade;

    @NotNull(message = "O prazo e obrigatorio")
    private LocalDate prazo;

    @NotNull(message = "O status e obrigatorio")
    private StatusTarefa status;

    public TarefaRequestDTO() {
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
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
}
