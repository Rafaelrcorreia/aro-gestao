package com.aro.aro_gestao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "compromissos")
public class Compromisso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // O cliente e opcional: um compromisso pode ser interno (ex.: reuniao da equipe)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    // A hora e opcional: sem hora, o compromisso vale para o dia inteiro
    @Column(name = "hora")
    private LocalTime hora;

    @Column(name = "descricao", length = 2000)
    private String descricao;

    public Compromisso() {
    }

    public Compromisso(Long id, Cliente cliente, String titulo, LocalDate data,
                       LocalTime hora, String descricao) {
        this.id = id;
        this.cliente = cliente;
        this.titulo = titulo;
        this.data = data;
        this.hora = hora;
        this.descricao = descricao;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
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
