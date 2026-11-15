package com.aro.aro_gestao.dto;

import com.aro.aro_gestao.entity.FormaPagamento;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// Usado para marcar um lancamento como pago/recebido
public class PagamentoRequestDTO {

    @NotNull(message = "A data do pagamento e obrigatoria")
    private LocalDate dataPagamento;

    private FormaPagamento formaPagamento;

    public PagamentoRequestDTO() {
    }

    public LocalDate getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDate dataPagamento) { this.dataPagamento = dataPagamento; }

    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(FormaPagamento formaPagamento) { this.formaPagamento = formaPagamento; }
}
