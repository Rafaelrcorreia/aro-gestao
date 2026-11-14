package com.aro.aro_gestao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public class CompromissoRequestDTO {

    // Opcional: so e validado quando vier preenchido
    @Positive(message = "O clienteId deve ser um numero positivo")
    private Long clienteId;

    @NotBlank(message = "O titulo e obrigatorio")
    @Size(max = 150, message = "O titulo deve ter no maximo 150 caracteres")
    private String titulo;

    @NotNull(message = "A data e obrigatoria")
    private LocalDate data;

    private LocalTime hora;

    @Size(max = 2000, message = "A descricao deve ter no maximo 2000 caracteres")
    private String descricao;

    public CompromissoRequestDTO() {
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

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
