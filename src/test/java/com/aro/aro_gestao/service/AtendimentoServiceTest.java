package com.aro.aro_gestao.service;

import com.aro.aro_gestao.dto.AtendimentoRequestDTO;
import com.aro.aro_gestao.dto.AtendimentoResponseDTO;
import com.aro.aro_gestao.entity.Atendimento;
import com.aro.aro_gestao.entity.Canal;
import com.aro.aro_gestao.entity.Cliente;
import com.aro.aro_gestao.exception.ResourceNotFoundException;
import com.aro.aro_gestao.repository.AtendimentoRepository;
import com.aro.aro_gestao.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtendimentoServiceTest {

    @Mock
    private AtendimentoRepository atendimentoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private AtendimentoService atendimentoService;

    private AtendimentoRequestDTO dtoValido;
    private Cliente clienteExistente;

    @BeforeEach
    void configurar() {
        clienteExistente = new Cliente();
        clienteExistente.setId(1L);
        clienteExistente.setNomeCompleto("Joao da Silva");

        dtoValido = new AtendimentoRequestDTO();
        dtoValido.setClienteId(1L);
        dtoValido.setDataHora(LocalDateTime.now());
        dtoValido.setCanal(Canal.WHATSAPP);
        dtoValido.setAssunto("Duvida sobre ITR");
        dtoValido.setDescricao("Cliente perguntou sobre prazo de pagamento do ITR");
    }

    @Test
    void deveCriarAtendimentoQuandoClienteExiste() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        when(atendimentoRepository.save(any(Atendimento.class))).thenAnswer(invocacao -> {
            Atendimento a = invocacao.getArgument(0);
            a.setId(10L);
            return a;
        });

        AtendimentoResponseDTO resultado = atendimentoService.criar(dtoValido);

        assertEquals(10L, resultado.getId());
        assertEquals("Joao da Silva", resultado.getNomeCliente());
        assertEquals(Canal.WHATSAPP, resultado.getCanal());
    }

    @Test
    void naoDeveCriarAtendimentoQuandoClienteNaoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());
        dtoValido.setClienteId(99L);

        assertThrows(ResourceNotFoundException.class, () -> atendimentoService.criar(dtoValido));
    }

    @Test
    void deveListarAtendimentosDoClienteEmOrdemDecrescente() {
        Atendimento maisRecente = new Atendimento();
        maisRecente.setId(2L);
        maisRecente.setCliente(clienteExistente);
        maisRecente.setDataHora(LocalDateTime.now());
        maisRecente.setCanal(Canal.TELEFONE);
        maisRecente.setAssunto("Retorno de ligacao");
        maisRecente.setDescricao("Cliente retornou a ligacao");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteExistente));
        when(atendimentoRepository.findByClienteIdOrderByDataHoraDesc(1L))
                .thenReturn(List.of(maisRecente));

        List<AtendimentoResponseDTO> resultado = atendimentoService.listarPorCliente(1L);

        assertEquals(1, resultado.size());
        assertEquals("Retorno de ligacao", resultado.get(0).getAssunto());
    }

    @Test
    void deveLancarExcecaoAoBuscarAtendimentoInexistente() {
        when(atendimentoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> atendimentoService.buscarPorId(999L));
    }
}