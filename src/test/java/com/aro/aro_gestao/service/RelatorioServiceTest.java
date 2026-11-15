package com.aro.aro_gestao.service;

import com.aro.aro_gestao.dto.RelatorioDTOs.QuantidadePorStatus;
import com.aro.aro_gestao.dto.RelatorioDTOs.RelatorioClientes;
import com.aro.aro_gestao.dto.RelatorioDTOs.RelatorioDocumentos;
import com.aro.aro_gestao.dto.RelatorioDTOs.RelatorioFinanceiro;
import com.aro.aro_gestao.dto.RelatorioDTOs.RelatorioProcessos;
import com.aro.aro_gestao.entity.CategoriaLancamento;
import com.aro.aro_gestao.entity.Cliente;
import com.aro.aro_gestao.entity.Documento;
import com.aro.aro_gestao.entity.Lancamento;
import com.aro.aro_gestao.entity.Processo;
import com.aro.aro_gestao.entity.StatusCliente;
import com.aro.aro_gestao.entity.StatusProcesso;
import com.aro.aro_gestao.entity.TipoDocumento;
import com.aro.aro_gestao.entity.TipoLancamento;
import com.aro.aro_gestao.entity.TipoProcesso;
import com.aro.aro_gestao.exception.BusinessException;
import com.aro.aro_gestao.repository.ClienteRepository;
import com.aro.aro_gestao.repository.DocumentoRepository;
import com.aro.aro_gestao.repository.ImovelRepository;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock
    private LancamentoRepository lancamentoRepository;

    @Mock
    private ProcessoRepository processoRepository;

    @Mock
    private DocumentoRepository documentoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ImovelRepository imovelRepository;

    @InjectMocks
    private RelatorioService relatorioService;

    private Cliente joao;
    private Cliente maria;

    @BeforeEach
    void configurar() {
        joao = cliente(1L, "Joao da Silva");
        maria = cliente(2L, "Maria Souza");
    }

    private static Cliente cliente(Long id, String nome) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNomeCompleto(nome);
        cliente.setStatus(StatusCliente.ATIVO);
        return cliente;
    }

    private static Lancamento lancamento(TipoLancamento tipo, CategoriaLancamento categoria, String valor, Cliente cliente) {
        Lancamento lancamento = new Lancamento();
        lancamento.setTipo(tipo);
        lancamento.setCategoria(categoria);
        lancamento.setDescricao("x");
        lancamento.setValor(new BigDecimal(valor));
        lancamento.setDataVencimento(LocalDate.of(2026, 11, 1));
        lancamento.setDataPagamento(LocalDate.of(2026, 11, 5));
        lancamento.setCliente(cliente);
        return lancamento;
    }

    @Test
    void deveTotalizarFinanceiroPorCategoriaECliente() {
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fim = LocalDate.of(2026, 11, 30);
        when(lancamentoRepository.buscarPagosNoPeriodo(inicio, fim)).thenReturn(List.of(
                lancamento(TipoLancamento.RECEITA, CategoriaLancamento.HONORARIOS, "1000.00", joao),
                lancamento(TipoLancamento.RECEITA, CategoriaLancamento.HONORARIOS, "500.00", joao),
                lancamento(TipoLancamento.RECEITA, CategoriaLancamento.REEMBOLSO_TAXAS, "2000.00", maria),
                lancamento(TipoLancamento.DESPESA, CategoriaLancamento.DESLOCAMENTO, "300.50", null)));
        when(lancamentoRepository.somarEmAbertoNoPeriodo(TipoLancamento.RECEITA, inicio, fim)).thenReturn(new BigDecimal("700.00"));
        when(lancamentoRepository.somarEmAbertoNoPeriodo(TipoLancamento.DESPESA, inicio, fim)).thenReturn(BigDecimal.ZERO);

        RelatorioFinanceiro rel = relatorioService.financeiro(inicio, fim);

        assertEquals(new BigDecimal("3500.00"), rel.totalRecebido());
        assertEquals(new BigDecimal("300.50"), rel.totalPago());
        assertEquals(new BigDecimal("3199.50"), rel.saldo());
        assertEquals(new BigDecimal("700.00"), rel.aReceberNoPeriodo());
        // Receitas antes das despesas; dentro do tipo, maior total primeiro
        assertEquals(CategoriaLancamento.REEMBOLSO_TAXAS, rel.porCategoria().get(0).categoria());
        assertEquals(CategoriaLancamento.HONORARIOS, rel.porCategoria().get(1).categoria());
        assertEquals(2, rel.porCategoria().get(1).quantidade());
        assertEquals(new BigDecimal("1500.00"), rel.porCategoria().get(1).total());
        assertEquals(TipoLancamento.DESPESA, rel.porCategoria().get(2).tipo());
        // Por cliente so conta receitas
        assertEquals(2, rel.recebidoPorCliente().size());
        assertEquals("Maria Souza", rel.recebidoPorCliente().get(0).nomeCliente());
        assertEquals(new BigDecimal("1500.00"), rel.recebidoPorCliente().get(1).total());
        assertEquals(4, rel.lancamentos().size());
    }

    @Test
    void deveValidarPeriodoDoFinanceiro() {
        LocalDate hoje = LocalDate.of(2026, 11, 15);
        assertThrows(BusinessException.class, () -> relatorioService.financeiro(null, hoje));
        assertThrows(BusinessException.class, () -> relatorioService.financeiro(hoje, hoje.minusDays(1)));
        assertThrows(BusinessException.class, () -> relatorioService.financeiro(hoje.minusYears(11), hoje));
    }

    private static Processo processo(StatusProcesso status, TipoProcesso tipo, LocalDate prazo) {
        Processo processo = new Processo();
        processo.setCliente(new Cliente());
        processo.getCliente().setId(1L);
        processo.setStatus(status);
        processo.setTipo(tipo);
        processo.setDataAbertura(LocalDate.of(2026, 1, 1));
        processo.setPrazoPrevisto(prazo);
        return processo;
    }

    @Test
    void deveResumirProcessosEOrdenarPorPrazo() {
        LocalDate hoje = LocalDate.now();
        when(processoRepository.buscarTodosComVinculos()).thenReturn(new ArrayList<>(List.of(
                processo(StatusProcesso.DEFERIDO, TipoProcesso.CAR, hoje.minusDays(30)),
                processo(StatusProcesso.EM_ANALISE, TipoProcesso.GEORREFERENCIAMENTO, null),
                processo(StatusProcesso.EXIGENCIA, TipoProcesso.GEORREFERENCIAMENTO, hoje.minusDays(2)),
                processo(StatusProcesso.PROTOCOLADO, TipoProcesso.CAR, hoje.plusDays(10)))));

        RelatorioProcessos rel = relatorioService.processos();

        assertEquals(4, rel.total());
        assertEquals(3, rel.emAndamento());
        assertEquals(1, rel.comPrazoVencido());
        // Todos os status aparecem, mesmo com zero
        assertEquals(StatusProcesso.values().length, rel.porStatus().size());
        assertEquals(0, rel.porStatus().stream().filter(s -> s.status() == StatusProcesso.ARQUIVADO)
                .mapToLong(QuantidadePorStatus::quantidade).sum());
        // Prazo mais proximo primeiro; sem prazo por ultimo
        assertEquals(hoje.minusDays(2), rel.processosEmAndamento().get(0).getPrazoPrevisto());
        assertNull(rel.processosEmAndamento().get(2).getPrazoPrevisto());
        assertEquals(2, rel.porTipo().get(0).total());
    }

    @Test
    void deveListarDocumentosVencidosEVencendo() {
        LocalDate hoje = LocalDate.now();
        Documento vencido = documento(hoje.minusDays(3));
        Documento vencendo = documento(hoje.plusDays(10));
        when(documentoRepository.findByDataValidadeLessThanEqualOrderByDataValidadeAscIdAsc(hoje.plusDays(30)))
                .thenReturn(List.of(vencido, vencendo));

        RelatorioDocumentos rel = relatorioService.documentos(null);

        assertEquals(30, rel.dias());
        assertEquals(1, rel.vencidos());
        assertEquals(1, rel.vencendo());
        assertThrows(BusinessException.class, () -> relatorioService.documentos(400));
    }

    private Documento documento(LocalDate validade) {
        Documento documento = new Documento();
        documento.setCliente(joao);
        documento.setTipo(TipoDocumento.CERTIDAO);
        documento.setTitulo("Certidao");
        documento.setDataValidade(validade);
        return documento;
    }

    @Test
    void deveMontarCarteiraDeClientesComTotais() {
        maria.setCidade("Barreiras");
        maria.setEstado("BA");
        when(clienteRepository.findAll()).thenReturn(List.of(maria, joao));
        when(imovelRepository.contarPorCliente()).thenReturn(List.<Object[]>of(new Object[]{1L, 2L}, new Object[]{2L, 1L}));
        when(processoRepository.contarEmAndamentoPorCliente(StatusProcesso.FINALIZADOS))
                .thenReturn(List.<Object[]>of(new Object[]{1L, 3L}));
        when(documentoRepository.contarVencidosPorCliente(any(LocalDate.class)))
                .thenReturn(List.<Object[]>of(new Object[]{2L, 1L}));
        when(lancamentoRepository.somarReceberEmAtrasoPorCliente(any(LocalDate.class)))
                .thenReturn(List.<Object[]>of(new Object[]{1L, new BigDecimal("800.00")}));

        RelatorioClientes rel = relatorioService.clientes();

        assertEquals(2, rel.totalClientes());
        assertEquals(3, rel.imoveis());
        assertEquals(3, rel.processosEmAndamento());
        assertEquals(1, rel.documentosVencidos());
        assertEquals(new BigDecimal("800.00"), rel.receberEmAtraso());
        // Ordenado por nome
        assertEquals("Joao da Silva", rel.clientes().get(0).nomeCliente());
        assertEquals(new BigDecimal("800.00"), rel.clientes().get(0).receberEmAtraso());
        assertEquals("Barreiras/BA", rel.clientes().get(1).cidadeUf());
        assertEquals(BigDecimal.ZERO, rel.clientes().get(1).receberEmAtraso());
        verify(processoRepository).contarEmAndamentoPorCliente(eq(StatusProcesso.FINALIZADOS));
    }
}
