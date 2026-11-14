package com.aro.aro_gestao.service;

import com.aro.aro_gestao.dto.CompromissoRequestDTO;
import com.aro.aro_gestao.dto.CompromissoResponseDTO;
import com.aro.aro_gestao.entity.Cliente;
import com.aro.aro_gestao.entity.Compromisso;
import com.aro.aro_gestao.exception.ResourceNotFoundException;
import com.aro.aro_gestao.repository.ClienteRepository;
import com.aro.aro_gestao.repository.CompromissoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompromissoServiceTest {

    @Mock
    private CompromissoRepository compromissoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private CompromissoService compromissoService;

    @Test
    void deveCriarCompromissoSemCliente() {
        CompromissoRequestDTO dto = criarDto(null);
        dto.setTitulo("  Reuniao interna  ");
        dto.setDescricao("   ");

        when(compromissoRepository.save(any(Compromisso.class)))
                .thenAnswer(chamada -> chamada.getArgument(0));

        CompromissoResponseDTO resultado = compromissoService.criar(dto);

        assertEquals("Reuniao interna", resultado.getTitulo());
        assertEquals(LocalDate.of(2026, 11, 20), resultado.getData());
        assertEquals(LocalTime.of(9, 0), resultado.getHora());
        assertNull(resultado.getDescricao());
        assertNull(resultado.getClienteId());
        assertNull(resultado.getNomeCliente());
        verifyNoInteractions(clienteRepository);
    }

    @Test
    void deveCriarCompromissoVinculadoAoCliente() {
        Cliente cliente = new Cliente();
        cliente.setId(5L);
        cliente.setNomeCompleto("Maria Silva");

        when(clienteRepository.findById(5L)).thenReturn(Optional.of(cliente));
        when(compromissoRepository.save(any(Compromisso.class)))
                .thenAnswer(chamada -> chamada.getArgument(0));

        CompromissoResponseDTO resultado = compromissoService.criar(criarDto(5L));

        assertEquals(5L, resultado.getClienteId());
        assertEquals("Maria Silva", resultado.getNomeCliente());
    }

    @Test
    void naoDeveCriarCompromissoParaClienteInexistente() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> compromissoService.criar(criarDto(99L)));

        verify(compromissoRepository, never()).save(any(Compromisso.class));
    }

    @Test
    void deveAtualizarCompromissoERemoverVinculoComCliente() {
        Cliente cliente = new Cliente();
        cliente.setId(5L);
        cliente.setNomeCompleto("Maria Silva");

        Compromisso existente = new Compromisso(1L, cliente, "Titulo antigo",
                LocalDate.of(2026, 11, 1), null, "Descricao antiga");

        when(compromissoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(compromissoRepository.save(any(Compromisso.class)))
                .thenAnswer(chamada -> chamada.getArgument(0));

        CompromissoRequestDTO dto = criarDto(null);
        dto.setDescricao("Levar a matricula do imovel");

        CompromissoResponseDTO resultado = compromissoService.atualizar(1L, dto);

        assertEquals(1L, resultado.getId());
        assertEquals("Visita ao cartorio", resultado.getTitulo());
        assertEquals("Levar a matricula do imovel", resultado.getDescricao());
        assertNull(resultado.getClienteId());
    }

    @Test
    void deveFalharAoBuscarAtualizarOuExcluirCompromissoInexistente() {
        when(compromissoRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> compromissoService.buscarPorId(404L));
        assertThrows(ResourceNotFoundException.class,
                () -> compromissoService.atualizar(404L, criarDto(null)));
        assertThrows(ResourceNotFoundException.class, () -> compromissoService.excluir(404L));

        verify(compromissoRepository, never()).save(any(Compromisso.class));
        verify(compromissoRepository, never()).delete(any(Compromisso.class));
    }

    @Test
    void deveExcluirCompromissoExistente() {
        Compromisso existente = new Compromisso(1L, null, "Visita ao cartorio",
                LocalDate.of(2026, 11, 20), null, null);
        when(compromissoRepository.findById(1L)).thenReturn(Optional.of(existente));

        compromissoService.excluir(1L);

        verify(compromissoRepository).delete(existente);
    }

    private CompromissoRequestDTO criarDto(Long clienteId) {
        CompromissoRequestDTO dto = new CompromissoRequestDTO();
        dto.setClienteId(clienteId);
        dto.setTitulo("Visita ao cartorio");
        dto.setData(LocalDate.of(2026, 11, 20));
        dto.setHora(LocalTime.of(9, 0));
        return dto;
    }
}
