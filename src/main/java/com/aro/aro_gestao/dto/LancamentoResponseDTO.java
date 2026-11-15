package com.aro.aro_gestao.dto;

import com.aro.aro_gestao.entity.CategoriaLancamento;
import com.aro.aro_gestao.entity.Cliente;
import com.aro.aro_gestao.entity.FormaPagamento;
import com.aro.aro_gestao.entity.Lancamento;
import com.aro.aro_gestao.entity.Processo;
import com.aro.aro_gestao.entity.TipoLancamento;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LancamentoResponseDTO {

    public enum Situacao {
        PAGO,
        PENDENTE,
        ATRASADO
    }

    private final Long id;
    private final TipoLancamento tipo;
    private final CategoriaLancamento categoria;
    private final String descricao;
    private final BigDecimal valor;
    private final LocalDate dataVencimento;
    private final LocalDate dataPagamento;
    private final FormaPagamento formaPagamento;
    private final Situacao situacao;
    private final Long clienteId;
    private final String nomeCliente;
    private final Long processoId;
    private final String descricaoProcesso;
    private final String observacoes;

    public LancamentoResponseDTO(Lancamento lancamento) {
        this.id = lancamento.getId();
        this.tipo = lancamento.getTipo();
        this.categoria = lancamento.getCategoria();
        this.descricao = lancamento.getDescricao();
        this.valor = lancamento.getValor();
        this.dataVencimento = lancamento.getDataVencimento();
        this.dataPagamento = lancamento.getDataPagamento();
        this.formaPagamento = lancamento.getFormaPagamento();
        this.situacao = calcularSituacao(lancamento.getDataVencimento(), lancamento.getDataPagamento(), LocalDate.now());

        Cliente cliente = lancamento.getCliente();
        this.clienteId = cliente == null ? null : cliente.getId();
        this.nomeCliente = cliente == null ? null : cliente.getNomeCompleto();

        Processo processo = lancamento.getProcesso();
        this.processoId = processo == null ? null : processo.getId();
        this.descricaoProcesso = processo == null ? null : descreverProcesso(processo);

        this.observacoes = lancamento.getObservacoes();
    }

    public static Situacao calcularSituacao(LocalDate vencimento, LocalDate pagamento, LocalDate hoje) {
        if (pagamento != null) {
            return Situacao.PAGO;
        }
        return vencimento.isBefore(hoje) ? Situacao.ATRASADO : Situacao.PENDENTE;
    }

    // Ex.: "Georreferenciamento - SIGEF-123"
    private static String descreverProcesso(Processo processo) {
        String descricao = processo.getTipo().getRotulo();
        if (processo.getNumeroProtocolo() != null) {
            descricao += " - " + processo.getNumeroProtocolo();
        }
        return descricao;
    }

    public Long getId() { return id; }
    public TipoLancamento getTipo() { return tipo; }
    public CategoriaLancamento getCategoria() { return categoria; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValor() { return valor; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public LocalDate getDataPagamento() { return dataPagamento; }
    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public Situacao getSituacao() { return situacao; }
    public Long getClienteId() { return clienteId; }
    public String getNomeCliente() { return nomeCliente; }
    public Long getProcessoId() { return processoId; }
    public String getDescricaoProcesso() { return descricaoProcesso; }
    public String getObservacoes() { return observacoes; }
}
