package com.aro.aro_gestao.service;

import com.aro.aro_gestao.dto.ItemChecklistRequestDTO;
import com.aro.aro_gestao.dto.ItemChecklistResponseDTO;
import com.aro.aro_gestao.dto.TarefaRequestDTO;
import com.aro.aro_gestao.dto.TarefaResponseDTO;
import com.aro.aro_gestao.entity.Cliente;
import com.aro.aro_gestao.entity.ItemChecklist;
import com.aro.aro_gestao.entity.Prioridade;
import com.aro.aro_gestao.entity.StatusTarefa;
import com.aro.aro_gestao.entity.Tarefa;
import com.aro.aro_gestao.exception.BusinessException;
import com.aro.aro_gestao.exception.ResourceNotFoundException;
import com.aro.aro_gestao.repository.ClienteRepository;
import com.aro.aro_gestao.repository.ItemChecklistRepository;
import com.aro.aro_gestao.repository.TarefaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ItemChecklistRepository itemChecklistRepository;

    @InjectMocks
    private TarefaService tarefaService;

    private TarefaRequestDTO dtoValido;
    private Cliente clienteExistente;

    @BeforeEach
    void configurar() {
        clienteExistente = new Cliente();
        clienteExistente.setId(1L);
        clienteExistente.setNomeCompleto("Joao da Silva");

        dtoValido = new TarefaRequestDTO();
        dtoValido.setClienteId(1L);
        dtoValido.setTitulo("Enviar documentacao do ITR");
        dtoValido.setDescricao("Reunir e enviar os documentos pendentes");
        dtoValido.setResponsavel("Ana");
        dtoValido.setPrioridade(Prioridade.ALTA);
        dtoValido.setPrazo(LocalDate.now().plusDays(5));
        dtoValido.setStatus(StatusTarefa.PENDENTE);
    }

    @Test
    void deveCriarTarefaQuandoClienteExiste() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(invocacao -> {
            Tarefa t = invocacao.getArgument(0);
            t.setId(10L);
            return t;
        });

        TarefaResponseDTO resultado = tarefaService.criar(dtoValido);

        assertEquals(10L, resultado.getId());
        assertEquals("Joao da Silva", resultado.getNomeCliente());
        assertEquals(Prioridade.ALTA, resultado.getPrioridade());
    }

    @Test
    void naoDeveCriarTarefaQuandoClienteNaoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());
        dtoValido.setClienteId(99L);

        assertThrows(ResourceNotFoundException.class, () -> tarefaService.criar(dtoValido));
    }

    @Test
    void deveMarcarTarefaComoAtrasadaQuandoPrazoPassouENaoConcluida() {
        Tarefa tarefa = new Tarefa();
        tarefa.setId(2L);
        tarefa.setCliente(clienteExistente);
        tarefa.setTitulo("Tarefa vencida");
        tarefa.setResponsavel("Ana");
        tarefa.setPrioridade(Prioridade.MEDIA);
        tarefa.setPrazo(LocalDate.now().minusDays(3));
        tarefa.setStatus(StatusTarefa.PENDENTE);

        TarefaResponseDTO resultado = new TarefaResponseDTO(tarefa);

        assertTrue(resultado.isAtrasada());
    }

    @Test
    void deveAlternarConclusaoDoItemChecklist() {
        Tarefa tarefa = new Tarefa();
        tarefa.setId(3L);

        ItemChecklist item = new ItemChecklist();
        item.setId(20L);
        item.setTarefa(tarefa);
        item.setDescricao("Separar documentos");
        item.setConcluido(false);

        when(tarefaRepository.findById(3L)).thenReturn(Optional.of(tarefa));
        when(itemChecklistRepository.findById(20L)).thenReturn(Optional.of(item));
        when(itemChecklistRepository.save(any(ItemChecklist.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        ItemChecklistResponseDTO resultado = tarefaService.alternarConclusaoItem(3L, 20L);

        assertTrue(resultado.isConcluido());
    }

    @Test
    void naoDeveAlternarItemQueNaoPertenceATarefa() {
        Tarefa tarefaCorreta = new Tarefa();
        tarefaCorreta.setId(3L);

        Tarefa outraTarefa = new Tarefa();
        outraTarefa.setId(4L);

        ItemChecklist item = new ItemChecklist();
        item.setId(20L);
        item.setTarefa(outraTarefa);

        when(tarefaRepository.findById(3L)).thenReturn(Optional.of(tarefaCorreta));
        when(itemChecklistRepository.findById(20L)).thenReturn(Optional.of(item));

        assertThrows(BusinessException.class, () -> tarefaService.alternarConclusaoItem(3L, 20L));
    }

    @Test
    void deveLancarExcecaoAoBuscarTarefaInexistente() {
        when(tarefaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tarefaService.buscarPorId(999L));
    }
}