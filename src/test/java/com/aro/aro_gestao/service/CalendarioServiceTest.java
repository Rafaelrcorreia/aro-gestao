package com.aro.aro_gestao.service;

import com.aro.aro_gestao.dto.CalendarioResponseDTO;
import com.aro.aro_gestao.entity.Atendimento;
import com.aro.aro_gestao.entity.Cliente;
import com.aro.aro_gestao.entity.Compromisso;
import com.aro.aro_gestao.entity.Imovel;
import com.aro.aro_gestao.entity.Processo;
import com.aro.aro_gestao.entity.StatusProcesso;
import com.aro.aro_gestao.entity.TipoProcesso;
import com.aro.aro_gestao.entity.StatusTarefa;
import com.aro.aro_gestao.entity.Tarefa;
import com.aro.aro_gestao.exception.BusinessException;
import com.aro.aro_gestao.exception.ResourceNotFoundException;
import com.aro.aro_gestao.repository.AtendimentoRepository;
import com.aro.aro_gestao.repository.ClienteRepository;
import com.aro.aro_gestao.repository.CompromissoRepository;
import com.aro.aro_gestao.repository.ProcessoRepository;
import com.aro.aro_gestao.repository.TarefaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarioServiceTest {
    @Mock private TarefaRepository tarefaRepository;
    @Mock private AtendimentoRepository atendimentoRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private CompromissoRepository compromissoRepository;
    @Mock private ProcessoRepository processoRepository;
    @InjectMocks private CalendarioService calendarioService;

    @Test
    void deveRetornarMesVazioComLimitesDeAnoBissexto() {
        CalendarioResponseDTO resultado = calendarioService.buscarMes("2028-02", null);
        assertEquals("2028-02", resultado.getMes());
        assertEquals(LocalDate.of(2028, 2, 1), resultado.getInicio());
        assertEquals(LocalDate.of(2028, 2, 29), resultado.getFim());
        assertTrue(resultado.getEventos().isEmpty());
        verify(tarefaRepository).findByPrazoBetweenOrderByPrazoAscIdAsc(
                resultado.getInicio(), resultado.getFim());
        verify(atendimentoRepository).findByProximoContatoBetweenOrderByProximoContatoAscIdAsc(
                resultado.getInicio(), resultado.getFim());
        verify(compromissoRepository).findByDataBetweenOrderByDataAscIdAsc(
                resultado.getInicio(), resultado.getFim());
        verify(processoRepository).findByPrazoPrevistoBetweenOrderByPrazoPrevistoAscIdAsc(
                resultado.getInicio(), resultado.getFim());
        verifyNoInteractions(clienteRepository);
    }

    @Test
    void deveIncluirCompromissosComESemCliente() {
        Cliente cliente = new Cliente();
        cliente.setId(3L);
        cliente.setNomeCompleto("Cliente de teste");

        Compromisso reuniao = new Compromisso(7L, cliente, "Reuniao no cartorio",
                LocalDate.of(2026, 10, 15), LocalTime.of(14, 30), null);
        Compromisso interno = new Compromisso(8L, null, "Reuniao interna",
                LocalDate.of(2026, 10, 16), null, null);

        when(compromissoRepository.findByDataBetweenOrderByDataAscIdAsc(
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)))
                .thenReturn(List.of(interno, reuniao));

        CalendarioResponseDTO resultado = calendarioService.buscarMes("2026-10", null);

        assertEquals(List.of("COMPROMISSO:7", "COMPROMISSO:8"),
                resultado.getEventos().stream().map(e -> e.getId()).toList());
        assertEquals(LocalTime.of(14, 30), resultado.getEventos().getFirst().getHora());
        assertEquals("Cliente de teste", resultado.getEventos().getFirst().getNomeCliente());
        assertNull(resultado.getEventos().getLast().getClienteId());
        assertNull(resultado.getEventos().getLast().getNomeCliente());
        assertNull(resultado.getEventos().getLast().getHora());
    }

    @Test
    void deveUnificarEventosOrdenarEDistinguirIdsDasOrigens() {
        Cliente cliente = new Cliente();
        cliente.setId(3L);
        cliente.setNomeCompleto("Cliente de teste");

        Tarefa primeira = tarefa(1L, LocalDate.of(2026, 10, 1), cliente);
        Tarefa ultima = tarefa(2L, LocalDate.of(2026, 10, 31), cliente);
        ultima.setStatus(StatusTarefa.CONCLUIDA);
        Atendimento retorno = new Atendimento();
        retorno.setId(1L);
        retorno.setCliente(cliente);
        retorno.setAssunto("Retornar documentos");
        retorno.setProximoContato(LocalDate.of(2026, 10, 1));

        when(tarefaRepository.findByPrazoBetweenOrderByPrazoAscIdAsc(
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)))
                .thenReturn(List.of(ultima, primeira));
        when(atendimentoRepository.findByProximoContatoBetweenOrderByProximoContatoAscIdAsc(
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)))
                .thenReturn(List.of(retorno));

        CalendarioResponseDTO resultado = calendarioService.buscarMes("2026-10", null);
        assertEquals(List.of("RETORNO:1", "TAREFA:1", "TAREFA:2"),
                resultado.getEventos().stream().map(e -> e.getId()).toList());
        assertEquals("Cliente de teste", resultado.getEventos().getFirst().getNomeCliente());
        assertEquals(3L, resultado.getEventos().getFirst().getClienteId());
        assertNull(resultado.getEventos().getFirst().getStatusTarefa());
        assertEquals(StatusTarefa.CONCLUIDA, resultado.getEventos().getLast().getStatusTarefa());
    }

    @Test
    void deveUsarConsultasRestritasAoClienteSelecionado() {
        when(clienteRepository.existsById(3L)).thenReturn(true);
        calendarioService.buscarMes("2026-12", 3L);
        verify(tarefaRepository).findByClienteIdAndPrazoBetweenOrderByPrazoAscIdAsc(
                3L, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31));
        verify(atendimentoRepository).findByClienteIdAndProximoContatoBetweenOrderByProximoContatoAscIdAsc(
                3L, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31));
        verify(compromissoRepository).findByClienteIdAndDataBetweenOrderByDataAscIdAsc(
                3L, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31));
        verifyNoMoreInteractions(tarefaRepository, atendimentoRepository, compromissoRepository);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"2026-00", "2026-13", "2026-1", "26-10", "2026-10-01", " 2026-10",
            "0999-12", "10000-01", "abc"})
    void deveRejeitarMesInvalidoAntesDeConsultarBanco(String mes) {
        assertThrows(BusinessException.class, () -> calendarioService.buscarMes(mes, null));
        verifyNoInteractions(tarefaRepository, atendimentoRepository, clienteRepository, compromissoRepository);
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L})
    void deveRejeitarClienteIdNaoPositivo(Long id) {
        assertThrows(BusinessException.class, () -> calendarioService.buscarMes("2026-10", id));
        verifyNoInteractions(tarefaRepository, atendimentoRepository, clienteRepository, compromissoRepository);
    }

    @Test
    void deveRetornarErroParaClienteInexistente() {
        assertThrows(ResourceNotFoundException.class, () -> calendarioService.buscarMes("2026-10", 999L));
        verifyNoInteractions(tarefaRepository, atendimentoRepository, compromissoRepository);
    }

    private Tarefa tarefa(Long id, LocalDate data, Cliente cliente) {
        Tarefa tarefa = new Tarefa();
        tarefa.setId(id);
        tarefa.setTitulo("Prazo de documentos");
        tarefa.setCliente(cliente);
        tarefa.setPrazo(data);
        tarefa.setStatus(StatusTarefa.PENDENTE);
        return tarefa;
    }

    @Test
    void deveIncluirPrazosDeProcessosComTituloEStatus() {
        Cliente cliente = new Cliente();
        cliente.setId(3L);
        cliente.setNomeCompleto("Cliente de teste");

        Imovel imovel = new Imovel();
        imovel.setDenominacao("Fazenda Boa Vista");

        Processo comImovel = new Processo();
        comImovel.setId(11L);
        comImovel.setCliente(cliente);
        comImovel.setImovel(imovel);
        comImovel.setTipo(TipoProcesso.GEORREFERENCIAMENTO);
        comImovel.setStatus(StatusProcesso.EXIGENCIA);
        comImovel.setPrazoPrevisto(LocalDate.of(2026, 10, 20));

        Processo semImovel = new Processo();
        semImovel.setId(12L);
        semImovel.setCliente(cliente);
        semImovel.setTipo(TipoProcesso.RETIFICACAO_MATRICULA);
        semImovel.setStatus(StatusProcesso.DEFERIDO);
        semImovel.setPrazoPrevisto(LocalDate.of(2026, 10, 5));

        when(clienteRepository.existsById(3L)).thenReturn(true);
        when(processoRepository.findByClienteIdAndPrazoPrevistoBetweenOrderByPrazoPrevistoAscIdAsc(
                3L, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)))
                .thenReturn(List.of(semImovel, comImovel));

        CalendarioResponseDTO resultado = calendarioService.buscarMes("2026-10", 3L);

        assertEquals(List.of("PROCESSO:12", "PROCESSO:11"),
                resultado.getEventos().stream().map(e -> e.getId()).toList());
        assertEquals("Retificação de matrícula", resultado.getEventos().getFirst().getTitulo());
        assertEquals(StatusProcesso.DEFERIDO,
                resultado.getEventos().getFirst().getStatusProcesso());
        assertEquals("Georreferenciamento - Fazenda Boa Vista", resultado.getEventos().getLast().getTitulo());
        assertEquals("Cliente de teste", resultado.getEventos().getLast().getNomeCliente());
    }
}

