package com.aro.aro_gestao.dto;

import com.aro.aro_gestao.entity.StatusCliente;
import com.aro.aro_gestao.entity.TipoPessoa;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ClienteRequestDTO {

    @NotBlank(message = "O nome completo e obrigatorio")
    @Size(max = 150, message = "O nome completo deve ter no maximo 150 caracteres")
    private String nomeCompleto;

    @NotNull(message = "O tipo de pessoa e obrigatorio")
    private TipoPessoa tipoPessoa;

    @NotBlank(message = "O CPF ou CNPJ e obrigatorio")
    @Size(max = 20, message = "O CPF ou CNPJ deve ter no maximo 20 caracteres")
    private String cpfCnpj;

    @Size(max = 20, message = "O telefone deve ter no maximo 20 caracteres")
    private String telefone;

    @Size(max = 20, message = "O whatsapp deve ter no maximo 20 caracteres")
    private String whatsapp;

    @Email(message = "O e-mail informado nao e valido")
    @Size(max = 150, message = "O e-mail deve ter no maximo 150 caracteres")
    private String email;

    @Size(max = 200, message = "O endereco deve ter no maximo 200 caracteres")
    private String endereco;

    @Size(max = 100, message = "A cidade deve ter no maximo 100 caracteres")
    private String cidade;

    @Size(max = 2, message = "O estado deve ser a sigla com 2 letras")
    private String estado;

    @Size(max = 1000, message = "As observacoes devem ter no maximo 1000 caracteres")
    private String observacoes;

    @NotNull(message = "O status e obrigatorio")
    private StatusCliente status;

    public ClienteRequestDTO() {
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

    public StatusCliente getStatus() {
        return status;
    }

    public void setStatus(StatusCliente status) {
        this.status = status;
    }
}