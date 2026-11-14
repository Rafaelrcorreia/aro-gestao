package com.aro.aro_gestao.dto;

import com.aro.aro_gestao.entity.Prioridade;
import com.aro.aro_gestao.entity.StatusTarefa;
import java.time.LocalDate;
import java.time.LocalTime;

public class EventoCalendarioResponseDTO {
    private final String id;
    private final String tipo;
    private final Long origemId;
    private final LocalDate data;
    private final LocalTime hora;
    private final String titulo;
    private final Long clienteId;
    private final String nomeCliente;
    private final StatusTarefa statusTarefa;
    private final Prioridade prioridade;

    // Usado por tarefas e retornos, que nao tem hora
    public EventoCalendarioResponseDTO(String tipo, Long origemId, LocalDate data,
                                       String titulo, Long clienteId, String nomeCliente,
                                       StatusTarefa statusTarefa, Prioridade prioridade) {
        this(tipo, origemId, data, null, titulo, clienteId, nomeCliente, statusTarefa, prioridade);
    }

    public EventoCalendarioResponseDTO(String tipo, Long origemId, LocalDate data, LocalTime hora,
                                       String titulo, Long clienteId, String nomeCliente,
                                       StatusTarefa statusTarefa, Prioridade prioridade) {
        this.id = tipo + ":" + origemId;
        this.tipo = tipo;
        this.origemId = origemId;
        this.data = data;
        this.hora = hora;
        this.titulo = titulo;
        this.clienteId = clienteId;
        this.nomeCliente = nomeCliente;
        this.statusTarefa = statusTarefa;
        this.prioridade = prioridade;
    }

    public String getId() { return id; }
    public String getTipo() { return tipo; }
    public Long getOrigemId() { return origemId; }
    public LocalDate getData() { return data; }
    public LocalTime getHora() { return hora; }
    public String getTitulo() { return titulo; }
    public Long getClienteId() { return clienteId; }
    public String getNomeCliente() { return nomeCliente; }
    public StatusTarefa getStatusTarefa() { return statusTarefa; }
    public Prioridade getPrioridade() { return prioridade; }
}
