package com.aro.aro_gestao.service;

import com.aro.aro_gestao.dto.ClienteRequestDTO;
import com.aro.aro_gestao.dto.ClienteResponseDTO;
import com.aro.aro_gestao.entity.Cliente;
import com.aro.aro_gestao.entity.StatusCliente;
import com.aro.aro_gestao.entity.TipoPessoa;
import com.aro.aro_gestao.exception.BusinessException;
import com.aro.aro_gestao.exception.ResourceNotFoundException;
import com.aro.aro_gestao.repository.AtendimentoRepository;
import com.aro.aro_gestao.repository.ClienteRepository;
import com.aro.aro_gestao.repository.CompromissoRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private AtendimentoRepository atendimentoRepository;

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private CompromissoRepository compromissoRepository;

    @InjectMocks
    private ClienteService clienteService;

    private ClienteRequestDTO dtoValido;

    @BeforeEach
    void configurar() {
        dtoValido = new ClienteRequestDTO();
        dtoValido.setNomeCompleto("Joao da Silva");
        dtoValido.setTipoPessoa(TipoPessoa.FISICA);
        dtoValido.setCpfCnpj("12345678900");
        dtoValido.setTelefone("77999990000");
        dtoValido.setStatus(StatusCliente.ATIVO);
    }

    @Test
    void deveCriarClienteComSucessoQuandoCpfCnpjNaoExiste() {
        when(clienteRepository.findByCpfCnpj("12345678900")).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocacao -> {
            Cliente c = invocacao.getArgument(0);
            c.setId(1L);
            return c;
        });

        ClienteResponseDTO resultado = clienteService.criar(dtoValido);

        assertEquals("Joao da Silva", resultado.getNomeCompleto());
        assertEquals(1L, resultado.getId());
    }

    @Test
    void naoDeveCriarClienteComCpfCnpjDuplicado() {
        Cliente existente = new Cliente();
        existente.setId(5L);
        existente.setCpfCnpj("12345678900");

        when(clienteRepository.findByCpfCnpj("12345678900")).thenReturn(Optional.of(existente));

        assertThrows(BusinessException.class, () -> clienteService.criar(dtoValido));
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void deveLancarExcecaoQuandoClienteNaoExisteAoBuscarPorId() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> clienteService.buscarPorId(99L));
    }

    @Test
    void deveAtualizarClienteExistente() {
        Cliente clienteExistente = new Cliente();
        clienteExistente.setId(1L);
        clienteExistente.setCpfCnpj("12345678900");
        clienteExistente.setDataCadastro(LocalDate.now());

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        when(clienteRepository.findByCpfCnpj("12345678900")).thenReturn(Optional.of(clienteExistente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        dtoValido.setNomeCompleto("Joao Atualizado");

        ClienteResponseDTO resultado = clienteService.atualizar(1L, dtoValido);

        assertEquals("Joao Atualizado", resultado.getNomeCompleto());
    }

    @Test
    void deveExcluirClienteSemVinculos() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        clienteService.excluir(1L);

        verify(clienteRepository).delete(cliente);
    }

    @Test
    void naoDeveExcluirClienteComVinculos() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(tarefaRepository.existsByClienteId(1L)).thenReturn(true);
        when(compromissoRepository.existsByClienteId(1L)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> clienteService.excluir(1L));

        assertTrue(ex.getMessage().contains("tarefas, compromissos"));
        verify(clienteRepository, never()).delete(any(Cliente.class));
    }
}
