package com.aro.aro_gestao.dto;

import com.aro.aro_gestao.entity.Imovel;
import com.aro.aro_gestao.entity.SituacaoGeorreferenciamento;

import java.math.BigDecimal;

public class ImovelResponseDTO {

    private final Long id;
    private final Long clienteId;
    private final String nomeCliente;
    private final String denominacao;
    private final String municipio;
    private final String estado;
    private final BigDecimal areaHectares;
    private final String matricula;
    private final String cartorio;
    private final String codigoCar;
    private final String codigoCcir;
    private final String nirf;
    private final SituacaoGeorreferenciamento situacaoGeorreferenciamento;
    private final String observacoes;

    public ImovelResponseDTO(Imovel imovel) {
        this.id = imovel.getId();
        this.clienteId = imovel.getCliente().getId();
        this.nomeCliente = imovel.getCliente().getNomeCompleto();
        this.denominacao = imovel.getDenominacao();
        this.municipio = imovel.getMunicipio();
        this.estado = imovel.getEstado();
        this.areaHectares = imovel.getAreaHectares();
        this.matricula = imovel.getMatricula();
        this.cartorio = imovel.getCartorio();
        this.codigoCar = imovel.getCodigoCar();
        this.codigoCcir = imovel.getCodigoCcir();
        this.nirf = imovel.getNirf();
        this.situacaoGeorreferenciamento = imovel.getSituacaoGeorreferenciamento();
        this.observacoes = imovel.getObservacoes();
    }

    public Long getId() { return id; }
    public Long getClienteId() { return clienteId; }
    public String getNomeCliente() { return nomeCliente; }
    public String getDenominacao() { return denominacao; }
    public String getMunicipio() { return municipio; }
    public String getEstado() { return estado; }
    public BigDecimal getAreaHectares() { return areaHectares; }
    public String getMatricula() { return matricula; }
    public String getCartorio() { return cartorio; }
    public String getCodigoCar() { return codigoCar; }
    public String getCodigoCcir() { return codigoCcir; }
    public String getNirf() { return nirf; }
    public SituacaoGeorreferenciamento getSituacaoGeorreferenciamento() { return situacaoGeorreferenciamento; }
    public String getObservacoes() { return observacoes; }
}
