package com.aro.aro_gestao.service;

import com.aro.aro_gestao.dto.DashboardResponseDTO;
import com.aro.aro_gestao.entity.Atendimento;
import com.aro.aro_gestao.entity.Cliente;
import com.aro.aro_gestao.entity.StatusCliente;
import com.aro.aro_gestao.entity.StatusTarefa;
import com.aro.aro_gestao.entity.Tarefa;
import com.aro.aro_gestao.repository.AtendimentoRepository;
import com.aro.aro_gestao.repository.ClienteRepository;
import com.aro.aro_gestao.repository.TarefaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private AtendimentoRepository atendimentoRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void deveRetornarZerosEListasVaziasSemDados() {
        DashboardResponseDTO resultado = dashboardService.buscarResumo();

        assertEquals(0L, resultado.getClientesAtivos());
        assertEquals(0L, resultado.getTarefasPendentes());
        assertEquals(0L, resultado.getTarefasAtrasadas());
        assertTrue(resultado.getProximosPrazos().isEmpty());
        assertTrue(resultado.getUltimosAtendimentos().isEmpty());
    }

    @Test
    void deveConsultarIndicadoresComMesmaDataReferencia() {
        when(clienteRepository.countByStatus(StatusCliente.ATIVO)).thenReturn(8L);
        when(tarefaRepository.countByStatusNotAndPrazoGreaterThanEqual(
                eq(StatusTarefa.CONCLUIDA), any(LocalDate.class))).thenReturn(6L);
        when(tarefaRepository.countByStatusNotAndPrazoBefore(
                eq(StatusTarefa.CONCLUIDA), any(LocalDate.class))).thenReturn(2L);

        LocalDate antes = LocalDate.now();
        DashboardResponseDTO resultado = dashboardService.buscarResumo();
        LocalDate depois = LocalDate.now();

        assertEquals(8L, resultado.getClientesAtivos());
        assertEquals(6L, resultado.getTarefasPendentes());
        assertEquals(2L, resultado.getTarefasAtrasadas());
        assertTrue(!resultado.getDataReferencia().isBefore(antes)
                && !resultado.getDataReferencia().isAfter(depois));
        verify(tarefaRepository).countByStatusNotAndPrazoBefore(
                StatusTarefa.CONCLUIDA, resultado.getDataReferencia());
        verify(tarefaRepository).countByStatusNotAndPrazoGreaterThanEqual(
                StatusTarefa.CONCLUIDA, resultado.getDataReferencia());
        verify(tarefaRepository)
                .findTop5ByStatusNotAndPrazoGreaterThanEqualOrderByPrazoAscIdAsc(
                        StatusTarefa.CONCLUIDA, resultado.getDataReferencia());
    }

    @Test
    void deveRetornarDadosDoClienteNasListas() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNomeCompleto("Maria Silva");

        Tarefa tarefa = new Tarefa();
        tarefa.setId(10L);
        tarefa.setCliente(cliente);
        tarefa.setTitulo("Entregar documentos");
        tarefa.setStatus(StatusTarefa.PENDENTE);
        tarefa.setPrazo(LocalDate.now().plusDays(1));

        Atendimento atendimento = new Atendimento();
        atendimento.setId(20L);
        atendimento.setCliente(cliente);
        atendimento.setAssunto("Orientacao sobre documentos");

        when(tarefaRepository
                .findTop5ByStatusNotAndPrazoGreaterThanEqualOrderByPrazoAscIdAsc(
                        eq(StatusTarefa.CONCLUIDA), any(LocalDate.class)))
                .thenReturn(List.of(tarefa));
        when(atendimentoRepository.findTop5ByOrderByDataHoraDescIdDesc())
                .thenReturn(List.of(atendimento));

        DashboardResponseDTO resultado = dashboardService.buscarResumo();

        assertEquals(1, resultado.getProximosPrazos().size());
        assertEquals(10L, resultado.getProximosPrazos().getFirst().getId());
        assertEquals("Maria Silva", resultado.getProximosPrazos().getFirst().getNomeCliente());
        assertEquals(1, resultado.getUltimosAtendimentos().size());
        assertEquals(20L, resultado.getUltimosAtendimentos().getFirst().getId());
        assertEquals(1L, resultado.getUltimosAtendimentos().getFirst().getClienteId());
        assertEquals("Maria Silva", resultado.getUltimosAtendimentos().getFirst().getNomeCliente());
    }
}
