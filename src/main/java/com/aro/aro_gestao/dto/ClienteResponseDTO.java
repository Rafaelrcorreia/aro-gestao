package com.aro.aro_gestao.dto;

import com.aro.aro_gestao.entity.Cliente;
import com.aro.aro_gestao.entity.StatusCliente;
import com.aro.aro_gestao.entity.TipoPessoa;

import java.time.LocalDate;

public class ClienteResponseDTO {

    private Long id;
    private String nomeCompleto;
    private TipoPessoa tipoPessoa;
    private String cpfCnpj;
    private String telefone;
    private String whatsapp;
    private String email;
    private String endereco;
    private String cidade;
    private String estado;
    private String observacoes;
    private LocalDate dataCadastro;
    private StatusCliente status;

    public ClienteResponseDTO() {
    }

    public ClienteResponseDTO(Cliente cliente) {
        this.id = cliente.getId();
        this.nomeCompleto = cliente.getNomeCompleto();
        this.tipoPessoa = cliente.getTipoPessoa();
        this.cpfCnpj = cliente.getCpfCnpj();
        this.telefone = cliente.getTelefone();
        this.whatsapp = cliente.getWhatsapp();
        this.email = cliente.getEmail();
        this.endereco = cliente.getEndereco();
        this.cidade = cliente.getCidade();
        this.estado = cliente.getEstado();
        this.observacoes = cliente.getObservacoes();
        this.dataCadastro = cliente.getDataCadastro();
        this.status = cliente.getStatus();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public TipoPessoa getTipoPessoa() {
        return tipoPessoa;
    }

    public void setTipoPessoa(TipoPessoa tipoPessoa) {
        this.tipoPessoa = tipoPessoa;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDate dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public StatusCliente getStatus() {
        return status;
    }

    public void setStatus(StatusCliente status) {
        this.status = status;
    }
}
