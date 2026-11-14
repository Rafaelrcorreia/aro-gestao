package com.aro.aro_gestao.dto;

import com.aro.aro_gestao.entity.SituacaoGeorreferenciamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ImovelRequestDTO {

    @NotNull(message = "O cliente e obrigatorio")
    @Positive(message = "O clienteId deve ser um numero positivo")
    private Long clienteId;

    @NotBlank(message = "A denominacao do imovel e obrigatoria")
    @Size(max = 150, message = "A denominacao deve ter no maximo 150 caracteres")
    private String denominacao;

    @NotBlank(message = "O municipio e obrigatorio")
    @Size(max = 100, message = "O municipio deve ter no maximo 100 caracteres")
    private String municipio;

    @NotBlank(message = "O estado e obrigatorio")
    @Pattern(regexp = "[A-Za-z]{2}", message = "O estado deve ser a sigla com 2 letras")
    private String estado;

    @DecimalMin(value = "0.0001", message = "A area deve ser maior que zero")
    @Digits(integer = 10, fraction = 4, message = "A area deve ter no maximo 10 digitos inteiros e 4 decimais")
    private BigDecimal areaHectares;

    @Size(max = 50, message = "A matricula deve ter no maximo 50 caracteres")
    private String matricula;

    @Size(max = 150, message = "O cartorio deve ter no maximo 150 caracteres")
    private String cartorio;

    @Size(max = 60, message = "O codigo do CAR deve ter no maximo 60 caracteres")
    private String codigoCar;

    @Size(max = 30, message = "O codigo do CCIR deve ter no maximo 30 caracteres")
    private String codigoCcir;

    @Size(max = 20, message = "O NIRF deve ter no maximo 20 caracteres")
    private String nirf;

    @NotNull(message = "A situacao do georreferenciamento e obrigatoria")
    private SituacaoGeorreferenciamento situacaoGeorreferenciamento;

    @Size(max = 1000, message = "As observacoes devem ter no maximo 1000 caracteres")
    private String observacoes;

    public ImovelRequestDTO() {
    }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public String getDenominacao() { return denominacao; }
    public void setDenominacao(String denominacao) { this.denominacao = denominacao; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public BigDecimal getAreaHectares() { return areaHectares; }
    public void setAreaHectares(BigDecimal areaHectares) { this.areaHectares = areaHectares; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getCartorio() { return cartorio; }
    public void setCartorio(String cartorio) { this.cartorio = cartorio; }

    public String getCodigoCar() { return codigoCar; }
    public void setCodigoCar(String codigoCar) { this.codigoCar = codigoCar; }

    public String getCodigoCcir() { return codigoCcir; }
    public void setCodigoCcir(String codigoCcir) { this.codigoCcir = codigoCcir; }

    public String getNirf() { return nirf; }
    public void setNirf(String nirf) { this.nirf = nirf; }

    public SituacaoGeorreferenciamento getSituacaoGeorreferenciamento() { return situacaoGeorreferenciamento; }
    public void setSituacaoGeorreferenciamento(SituacaoGeorreferenciamento situacaoGeorreferenciamento) {
        this.situacaoGeorreferenciamento = situacaoGeorreferenciamento;
    }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}
