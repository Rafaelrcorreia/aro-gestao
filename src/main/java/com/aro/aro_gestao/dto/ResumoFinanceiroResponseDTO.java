package com.aro.aro_gestao.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// Totais do mes. Todos os valores em reais.
public class ResumoFinanceiroResponseDTO {

    private final String mes;
    private final LocalDate inicio;
    private final LocalDate fim;
    // Recebido/pago = pelo dia do pagamento, dentro do mes
    private final BigDecimal recebido;
    private final BigDecimal pago;
    private final BigDecimal saldo;
    // Em aberto com vencimento dentro do mes
    private final BigDecimal totalAReceber;
    private final BigDecimal totalAPagar;
    // Em aberto com vencimento ja passado, de qualquer mes
    private final BigDecimal receberEmAtraso;
    private final BigDecimal pagarEmAtraso;

    public ResumoFinanceiroResponseDTO(String mes, LocalDate inicio, LocalDate fim,
                                       BigDecimal recebido, BigDecimal pago,
                                       BigDecimal totalAReceber, BigDecimal totalAPagar,
                                       BigDecimal receberEmAtraso, BigDecimal pagarEmAtraso) {
        this.mes = mes;
        this.inicio = inicio;
        this.fim = fim;
        this.recebido = recebido;
        this.pago = pago;
        this.saldo = recebido.subtract(pago);
        this.totalAReceber = totalAReceber;
        this.totalAPagar = totalAPagar;
        this.receberEmAtraso = receberEmAtraso;
        this.pagarEmAtraso = pagarEmAtraso;
    }

    public String getMes() { return mes; }
    public LocalDate getInicio() { return inicio; }
    public LocalDate getFim() { return fim; }
    public BigDecimal getRecebido() { return recebido; }
    public BigDecimal getPago() { return pago; }
    public BigDecimal getSaldo() { return saldo; }
    public BigDecimal getTotalAReceber() { return totalAReceber; }
    public BigDecimal getTotalAPagar() { return totalAPagar; }
    public BigDecimal getReceberEmAtraso() { return receberEmAtraso; }
    public BigDecimal getPagarEmAtraso() { return pagarEmAtraso; }
}
