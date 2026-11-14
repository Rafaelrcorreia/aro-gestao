package com.aro.aro_gestao.dto;

import com.aro.aro_gestao.entity.Cliente;
import com.aro.aro_gestao.entity.Compromisso;

import java.time.LocalDate;
import java.time.LocalTime;

public class CompromissoResponseDTO {

    private Long id;
    private Long clienteId;
    private String nomeCliente;
    private String titulo;
    private LocalDate data;
    private LocalTime hora;
    private String descricao;

    public CompromissoResponseDTO() {
    }

    public CompromissoResponseDTO(Compromisso compromisso) {
        this.id = compromisso.getId();

        Cliente cliente = compromisso.getCliente();
        if (cliente != null) {
            this.clienteId = cliente.getId();
            this.nomeCliente = cliente.getNomeCompleto();
        }

        this.titulo = compromisso.getTitulo();
        this.data = compromisso.getData();
        this.hora = compromisso.getHora();
        this.descricao = compromisso.getDescricao();
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
