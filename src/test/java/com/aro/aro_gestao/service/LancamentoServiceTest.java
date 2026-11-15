package com.aro.aro_gestao.service;

import com.aro.aro_gestao.dto.LancamentoRequestDTO;
import com.aro.aro_gestao.dto.LancamentoResponseDTO;
import com.aro.aro_gestao.dto.LancamentoResponseDTO.Situacao;
import com.aro.aro_gestao.dto.PagamentoRequestDTO;
import com.aro.aro_gestao.dto.ResumoFinanceiroResponseDTO;
import com.aro.aro_gestao.entity.CategoriaLancamento;
import com.aro.aro_gestao.entity.Cliente;
import com.aro.aro_gestao.entity.FormaPagamento;
import com.aro.aro_gestao.entity.Lancamento;
import com.aro.aro_gestao.entity.Processo;
import com.aro.aro_gestao.entity.TipoLancamento;
import com.aro.aro_gestao.entity.TipoProcesso;
import com.aro.aro_gestao.exception.BusinessException;
import com.aro.aro_gestao.repository.ClienteRepository;
import com.aro.aro_gestao.repository.LancamentoRepository;
import com.aro.aro_gestao.repository.ProcessoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LancamentoServiceTest {

    @Mock
    private LancamentoRepository lancamentoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProcessoRepository processoRepository;

    @InjectMocks
    private LancamentoService lancamentoService;

    private Cliente cliente;
    private LancamentoRequestDTO dto;

    @BeforeEach
    void configurar() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNomeCompleto("Joao da Silva");

        dto = new LancamentoRequestDTO();
        dto.setTipo(TipoLancamento.RECEITA);
        dto.setCategoria(CategoriaLancamento.HONORARIOS);
        dto.setDescricao("  Honorarios georreferenciamento  ");
        dto.setValor(new BigDecimal("1500.00"));
        dto.setDataVencimento(LocalDate.of(2026, 11, 20));
        dto.setFormaPagamento(FormaPagamento.PIX);
        dto.setClienteId(1L);
    }

    private void salvarDevolvendoComId() {
        when(lancamentoRepository.save(any(Lancamento.class))).thenAnswer(invocacao -> {
            Lancamento lancamento = invocacao.getArgument(0);
            lancamento.setId(40L);
            return lancamento;
        });
    }

    @Test
    void deveCriarReceitaEmAbertoSemFormaDePagamento() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        salvarDevolvendoComId();

        LancamentoResponseDTO resultado = lancamentoService.criar(dto);

        assertEquals(40L, resultado.getId());
        assertEquals("Honorarios georreferenciamento", resultado.getDescricao());
        assertEquals("Joao da Silva", resultado.getNomeCliente());
        // Sem data de pagamento, a forma enviada e descartada
        assertNull(resultado.getFormaPagamento());
        assertNull(resultado.getDataPagamento());
    }

    @Test
    void deveCriarDespesaSemCliente() {
        dto.setTipo(TipoLancamento.DESPESA);
        dto.setCategoria(CategoriaLancamento.DESLOCAMENTO);
        dto.setClienteId(null);
        dto.setDataPagamento(LocalDate.now());
        salvarDevolvendoComId();

        LancamentoResponseDTO resultado = lancamentoService.criar(dto);

        assertNull(resultado.getClienteId());
        assertEquals(FormaPagamento.PIX, resultado.getFormaPagamento());
        assertEquals(Situacao.PAGO, resultado.getSituacao());
        verify(clienteRepository, never()).findById(any());
    }

    @Test
    void naoDeveAceitarPagamentoNoFuturo() {
        dto.setDataPagamento(LocalDate.now().plusDays(1));

        assertThrows(BusinessException.class, () -> lancamentoService.criar(dto));
        verify(lancamentoRepository, never()).save(any(Lancamento.class));
    }

    @Test
    void naoDeveAceitarProcessoSemClienteOuDeOutroCliente() {
        dto.setClienteId(null);
        dto.setProcessoId(6L);
        assertThrows(BusinessException.class, () -> lancamentoService.criar(dto));

        Cliente outro = new Cliente();
        outro.setId(2L);
        Processo processo = new Processo();
        processo.setCliente(outro);
        processo.setTipo(TipoProcesso.CAR);
        dto.setClienteId(1L);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(processoRepository.findById(6L)).thenReturn(Optional.of(processo));
        assertThrows(BusinessException.class, () -> lancamentoService.criar(dto));

        verify(lancamentoRepository, never()).save(any(Lancamento.class));
    }

    @Test
    void deveRegistrarEDesfazerPagamento() {
        Lancamento lancamento = new Lancamento();
        lancamento.setId(40L);
        lancamento.setTipo(TipoLancamento.RECEITA);
        lancamento.setDataVencimento(LocalDate.now().minusDays(5));
        when(lancamentoRepository.findComVinculosById(40L)).thenReturn(Optional.of(lancamento));
        when(lancamentoRepository.save(any(Lancamento.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        PagamentoRequestDTO pagamento = new PagamentoRequestDTO();
        pagamento.setDataPagamento(LocalDate.now());
        pagamento.setFormaPagamento(FormaPagamento.TRANSFERENCIA);
        LancamentoResponseDTO pago = lancamentoService.registrarPagamento(40L, pagamento);
        assertEquals(Situacao.PAGO, pago.getSituacao());
        assertEquals(FormaPagamento.TRANSFERENCIA, pago.getFormaPagamento());

        LancamentoResponseDTO reaberto = lancamentoService.desfazerPagamento(40L);
        assertEquals(Situacao.ATRASADO, reaberto.getSituacao());
        assertNull(reaberto.getFormaPagamento());
    }

    @Test
    void deveCalcularSituacao() {
        LocalDate hoje = LocalDate.of(2026, 11, 15);
        assertEquals(Situacao.PAGO, LancamentoResponseDTO.calcularSituacao(hoje.minusDays(9), hoje.minusDays(1), hoje));
        assertEquals(Situacao.ATRASADO, LancamentoResponseDTO.calcularSituacao(hoje.minusDays(1), null, hoje));
        assertEquals(Situacao.PENDENTE, LancamentoResponseDTO.calcularSituacao(hoje, null, hoje));
    }

    @Test
    void deveMontarResumoDoMesComSaldo() {
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fim = LocalDate.of(2026, 11, 30);
        when(lancamentoRepository.somarPagosNoPeriodo(TipoLancamento.RECEITA, inicio, fim)).thenReturn(new BigDecimal("3000.00"));
        when(lancamentoRepository.somarPagosNoPeriodo(TipoLancamento.DESPESA, inicio, fim)).thenReturn(new BigDecimal("450.50"));
        when(lancamentoRepository.somarEmAbertoNoPeriodo(TipoLancamento.RECEITA, inicio, fim)).thenReturn(new BigDecimal("1200.00"));
        when(lancamentoRepository.somarEmAbertoNoPeriodo(TipoLancamento.DESPESA, inicio, fim)).thenReturn(BigDecimal.ZERO);
        when(lancamentoRepository.somarAtrasados(eq(TipoLancamento.RECEITA), any(LocalDate.class))).thenReturn(new BigDecimal("800.00"));
        when(lancamentoRepository.somarAtrasados(eq(TipoLancamento.DESPESA), any(LocalDate.class))).thenReturn(BigDecimal.ZERO);

        ResumoFinanceiroResponseDTO resumo = lancamentoService.resumir("2026-11");

        assertEquals(new BigDecimal("2549.50"), resumo.getSaldo());
        assertEquals(new BigDecimal("1200.00"), resumo.getTotalAReceber());
        assertEquals(new BigDecimal("800.00"), resumo.getReceberEmAtraso());
        assertEquals(fim, resumo.getFim());
    }

    @Test
    void deveExigirMesValido() {
        assertThrows(BusinessException.class, () -> lancamentoService.resumir(null));
        assertThrows(BusinessException.class, () -> lancamentoService.listar("2026-13", null, null, null));
    }

    @Test
    void deveListarComFiltros() {
        when(lancamentoRepository.buscarNoPeriodo(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28),
                TipoLancamento.DESPESA, null, "taxa")).thenReturn(List.of());

        lancamentoService.listar("2026-02", TipoLancamento.DESPESA, null, "  taxa ");

        verify(lancamentoRepository).buscarNoPeriodo(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28),
                TipoLancamento.DESPESA, null, "taxa");
    }
}
