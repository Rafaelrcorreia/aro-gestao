package com.aro.aro_gestao.service;

import com.aro.aro_gestao.dto.ImovelRequestDTO;
import com.aro.aro_gestao.dto.ImovelResponseDTO;
import com.aro.aro_gestao.entity.Cliente;
import com.aro.aro_gestao.entity.Imovel;
import com.aro.aro_gestao.entity.SituacaoGeorreferenciamento;
import com.aro.aro_gestao.exception.ResourceNotFoundException;
import com.aro.aro_gestao.repository.ClienteRepository;
import com.aro.aro_gestao.repository.ImovelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImovelServiceTest {

    @Mock
    private ImovelRepository imovelRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ImovelService imovelService;

    private Cliente cliente;
    private ImovelRequestDTO dto;

    @BeforeEach
    void configurar() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNomeCompleto("Joao da Silva");

        dto = new ImovelRequestDTO();
        dto.setClienteId(1L);
        dto.setDenominacao("  Fazenda Boa Vista  ");
        dto.setMunicipio(" Barreiras ");
        dto.setEstado("ba");
        dto.setAreaHectares(new BigDecimal("125.5000"));
        dto.setMatricula("12.345");
        dto.setCartorio("   ");
        dto.setSituacaoGeorreferenciamento(SituacaoGeorreferenciamento.EM_ANDAMENTO);
    }

    @Test
    void deveCriarImovelNormalizandoOsCampos() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(imovelRepository.save(any(Imovel.class))).thenAnswer(invocacao -> {
            Imovel imovel = invocacao.getArgument(0);
            imovel.setId(10L);
            return imovel;
        });

        ImovelResponseDTO resultado = imovelService.criar(dto);

        assertEquals(10L, resultado.getId());
        assertEquals("Fazenda Boa Vista", resultado.getDenominacao());
        assertEquals("Barreiras", resultado.getMunicipio());
        assertEquals("BA", resultado.getEstado());
        assertEquals("Joao da Silva", resultado.getNomeCliente());
        assertEquals(new BigDecimal("125.5000"), resultado.getAreaHectares());
        // Campo opcional so com espacos vira null
        assertNull(resultado.getCartorio());
        assertNull(resultado.getCodigoCar());
    }

    @Test
    void naoDeveCriarImovelParaClienteInexistente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> imovelService.criar(dto));
        verify(imovelRepository, never()).save(any(Imovel.class));
    }

    @Test
    void deveListarComBuscaAparadaEFiltroDeCliente() {
        Imovel imovel = new Imovel();
        imovel.setId(10L);
        imovel.setCliente(cliente);
        imovel.setDenominacao("Fazenda Boa Vista");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(imovelRepository.buscar(1L, "boa")).thenReturn(List.of(imovel));

        List<ImovelResponseDTO> resultado = imovelService.listar(1L, "  boa ");

        assertEquals(1, resultado.size());
        assertEquals("Fazenda Boa Vista", resultado.get(0).getDenominacao());
    }

    @Test
    void deveListarTudoQuandoBuscaEmBranco() {
        when(imovelRepository.buscar(null, null)).thenReturn(List.of());

        assertEquals(0, imovelService.listar(null, "   ").size());
        verify(imovelRepository).buscar(null, null);
    }

    @Test
    void naoDeveListarParaClienteInexistente() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> imovelService.listar(99L, null));
    }

    @Test
    void deveAtualizarImovelTrocandoOCliente() {
        Cliente outroCliente = new Cliente();
        outroCliente.setId(2L);
        outroCliente.setNomeCompleto("Maria Souza");

        Imovel existente = new Imovel();
        existente.setId(10L);
        existente.setCliente(cliente);
        existente.setDenominacao("Nome antigo");

        dto.setClienteId(2L);
        when(imovelRepository.findById(10L)).thenReturn(Optional.of(existente));
        when(clienteRepository.findById(2L)).thenReturn(Optional.of(outroCliente));
        when(imovelRepository.save(any(Imovel.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        ImovelResponseDTO resultado = imovelService.atualizar(10L, dto);

        ArgumentCaptor<Imovel> captor = ArgumentCaptor.forClass(Imovel.class);
        verify(imovelRepository).save(captor.capture());
        assertEquals(2L, captor.getValue().getCliente().getId());
        assertEquals("Fazenda Boa Vista", resultado.getDenominacao());
        assertEquals(SituacaoGeorreferenciamento.EM_ANDAMENTO, resultado.getSituacaoGeorreferenciamento());
    }

    @Test
    void deveLancarExcecaoAoExcluirImovelInexistente() {
        when(imovelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> imovelService.excluir(99L));
        verify(imovelRepository, never()).delete(any(Imovel.class));
    }

    @Test
    void deveExcluirImovelExistente() {
        Imovel existente = new Imovel();
        existente.setId(10L);
        when(imovelRepository.findById(10L)).thenReturn(Optional.of(existente));

        imovelService.excluir(10L);

        verify(imovelRepository).delete(existente);
    }
}
