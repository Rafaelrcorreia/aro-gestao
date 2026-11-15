package com.aro.aro_gestao.dto;

import com.aro.aro_gestao.entity.CategoriaLancamento;
import com.aro.aro_gestao.entity.StatusCliente;
import com.aro.aro_gestao.entity.StatusProcesso;
import com.aro.aro_gestao.entity.TipoLancamento;
import com.aro.aro_gestao.entity.TipoProcesso;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Respostas dos relatorios (somente leitura, por isso records)
public final class RelatorioDTOs {

    private RelatorioDTOs() {
    }

    /* ---------- Financeiro ---------- */

    public record TotalPorCategoria(TipoLancamento tipo, CategoriaLancamento categoria,
                                    long quantidade, BigDecimal total) {
    }

    public record RecebidoPorCliente(Long clienteId, String nomeCliente, long quantidade, BigDecimal total) {
    }

    public record RelatorioFinanceiro(LocalDate inicio, LocalDate fim,
                                      BigDecimal totalRecebido, BigDecimal totalPago, BigDecimal saldo,
                                      BigDecimal aReceberNoPeriodo, BigDecimal aPagarNoPeriodo,
                                      List<TotalPorCategoria> porCategoria,
                                      List<RecebidoPorCliente> recebidoPorCliente,
                                      List<LancamentoResponseDTO> lancamentos) {
    }

    /* ---------- Processos ---------- */

    public record QuantidadePorStatus(StatusProcesso status, long quantidade) {
    }

    public record QuantidadePorTipo(TipoProcesso tipo, long total, long emAndamento) {
    }

    public record RelatorioProcessos(long total, long emAndamento, long comPrazoVencido,
                                     List<QuantidadePorStatus> porStatus,
                                     List<QuantidadePorTipo> porTipo,
                                     List<ProcessoResponseDTO> processosEmAndamento) {
    }

    /* ---------- Documentos ---------- */

    public record RelatorioDocumentos(LocalDate dataReferencia, int dias, LocalDate limite,
                                      long vencidos, long vencendo,
                                      List<DocumentoResponseDTO> documentos) {
    }

    /* ---------- Carteira de clientes ---------- */

    public record ResumoCliente(Long clienteId, String nomeCliente, StatusCliente status, String cidadeUf,
                                long imoveis, long processosEmAndamento, long documentosVencidos,
                                BigDecimal receberEmAtraso) {
    }

    public record RelatorioClientes(long totalClientes, long imoveis, long processosEmAndamento,
                                    long documentosVencidos, BigDecimal receberEmAtraso,
                                    List<ResumoCliente> clientes) {
    }
}
