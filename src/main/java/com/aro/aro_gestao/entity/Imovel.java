package com.aro.aro_gestao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "imoveis")
public class Imovel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Proprietario (ou cliente responsavel) do imovel
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // Nome da propriedade, ex.: "Fazenda Boa Vista"
    @Column(name = "denominacao", nullable = false, length = 150)
    private String denominacao;

    @Column(name = "municipio", nullable = false, length = 100)
    private String municipio;

    @Column(name = "estado", nullable = false, length = 2)
    private String estado;

    // Area total em hectares, com ate 4 casas decimais
    @Column(name = "area_hectares", precision = 14, scale = 4)
    private BigDecimal areaHectares;

    @Column(name = "matricula", length = 50)
    private String matricula;

    @Column(name = "cartorio", length = 150)
    private String cartorio;

    // Numero do recibo do CAR (Cadastro Ambiental Rural)
    @Column(name = "codigo_car", length = 60)
    private String codigoCar;

    // Codigo do imovel no CCIR/SNCR (INCRA)
    @Column(name = "codigo_ccir", length = 30)
    private String codigoCcir;

    // NIRF/CIB da Receita Federal (ITR)
    @Column(name = "nirf", length = 20)
    private String nirf;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao_georreferenciamento", nullable = false, length = 20)
    private SituacaoGeorreferenciamento situacaoGeorreferenciamento;

    @Column(name = "observacoes", length = 1000)
    private String observacoes;

    public Imovel() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

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
