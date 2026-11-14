package com.aro.aro_gestao.dto;

import com.aro.aro_gestao.entity.Atendimento;
import com.aro.aro_gestao.entity.Canal;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AtendimentoResponseDTO {

    private Long id;
    private Long clienteId;
    private String nomeCliente;
    private LocalDateTime dataHora;
    private Canal canal;
    private String assunto;
    private String descricao;
    private LocalDate proximoContato;

    public AtendimentoResponseDTO() {
    }

    public AtendimentoResponseDTO(Atendimento atendimento) {
        this.id = atendimento.getId();
        this.clienteId = atendimento.getCliente().getId();
        this.nomeCliente = atendimento.getCliente().getNomeCompleto();
        this.dataHora = atendimento.getDataHora();
        this.canal = atendimento.getCanal();
        this.assunto = atendimento.getAssunto();
        this.descricao = atendimento.getDescricao();
        this.proximoContato = atendimento.getProximoContato();
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

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public Canal getCanal() {
        return canal;
    }

    public void setCanal(Canal canal) {
        this.canal = canal;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDate getProximoContato() {
        return proximoContato;
    }

    public void setProximoContato(LocalDate proximoContato) {
        this.proximoContato = proximoContato;
    }
}