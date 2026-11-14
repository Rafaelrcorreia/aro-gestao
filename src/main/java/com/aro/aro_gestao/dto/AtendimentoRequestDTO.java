package com.aro.aro_gestao.dto;

import com.aro.aro_gestao.entity.Canal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AtendimentoRequestDTO {

    @NotNull(message = "O cliente e obrigatorio")
    private Long clienteId;

    @NotNull(message = "A data e hora do atendimento sao obrigatorias")
    private LocalDateTime dataHora;

    @NotNull(message = "O canal e obrigatorio")
    private Canal canal;

    @NotBlank(message = "O assunto e obrigatorio")
    @Size(max = 150, message = "O assunto deve ter no maximo 150 caracteres")
    private String assunto;

    @NotBlank(message = "A descricao e obrigatoria")
    @Size(max = 2000, message = "A descricao deve ter no maximo 2000 caracteres")
    private String descricao;

    private LocalDate proximoContato;

    public AtendimentoRequestDTO() {
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
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