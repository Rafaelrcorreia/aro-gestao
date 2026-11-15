package com.aro.aro_gestao.dto;

import com.aro.aro_gestao.entity.CategoriaLancamento;
import com.aro.aro_gestao.entity.FormaPagamento;
import com.aro.aro_gestao.entity.TipoLancamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LancamentoRequestDTO {

    @NotNull(message = "Informe se e receita ou despesa")
    private TipoLancamento tipo;

    @NotNull(message = "A categoria e obrigatoria")
    private CategoriaLancamento categoria;

    @NotBlank(message = "A descricao e obrigatoria")
    @Size(max = 150, message = "A descricao deve ter no maximo 150 caracteres")
    private String descricao;

    @NotNull(message = "O valor e obrigatorio")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
    @Digits(integer = 10, fraction = 2, message = "O valor deve ter no maximo 10 digitos e 2 casas decimais")
    private BigDecimal valor;

    @NotNull(message = "A data de vencimento e obrigatoria")
    private LocalDate dataVencimento;

    // Preenchida = ja pago/recebido
    private LocalDate dataPagamento;

    private FormaPagamento formaPagamento;

    @Positive(message = "O clienteId deve ser um numero positivo")
    private Long clienteId;

    @Positive(message = "O processoId deve ser um numero positivo")
    private Long processoId;

    @Size(max = 1000, message = "As observacoes devem ter no maximo 1000 caracteres")
    private String observacoes;

    public LancamentoRequestDTO() {
    }

    public TipoLancamento getTipo() { return tipo; }
    public void setTipo(TipoLancamento tipo) { this.tipo = tipo; }

    public CategoriaLancamento getCategoria() { return categoria; }
    public void setCategoria(CategoriaLancamento categoria) { this.categoria = categoria; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }

    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }

    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(FormaPagamento formaPagamento) { this.formaPagamento = formaPagamento; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public Long getProcessoId() { return processoId; }
    public void setProcessoId(Long processoId) { this.processoId = processoId; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}
