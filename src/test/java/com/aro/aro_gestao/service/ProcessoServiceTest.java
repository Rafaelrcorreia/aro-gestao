package com.aro.aro_gestao.service;

import com.aro.aro_gestao.dto.ProcessoRequestDTO;
import com.aro.aro_gestao.dto.ProcessoResponseDTO;
import com.aro.aro_gestao.entity.Cliente;
import com.aro.aro_gestao.entity.Imovel;
import com.aro.aro_gestao.entity.Processo;
import com.aro.aro_gestao.entity.StatusProcesso;
import com.aro.aro_gestao.entity.TipoProcesso;
import com.aro.aro_gestao.exception.BusinessException;
import com.aro.aro_gestao.exception.ResourceNotFoundException;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessoServiceTest {

    @Mock
    private ProcessoRepository processoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ImovelRepository imovelRepository;

    @Mock
    private DocumentoRepository documentoRepository;

    @Mock
    private LancamentoRepository lancamentoRepository;

    @InjectMocks
    private ProcessoService processoService;

    private Cliente cliente;
    private Imovel imovel;
    private ProcessoRequestDTO dto;

    @BeforeEach
    void configurar() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNomeCompleto("Joao da Silva");

        imovel = new Imovel();
        imovel.setId(5L);
        imovel.setCliente(cliente);
        imovel.setDenominacao("Fazenda Boa Vista");

        dto = new ProcessoRequestDTO();
        dto.setClienteId(1L);
        dto.setImovelId(5L);
        dto.setTipo(TipoProcesso.GEORREFERENCIAMENTO);
        dto.setStatus(StatusProcesso.PROTOCOLADO);
        dto.setNumeroProtocolo("  SIGEF-123  ");
        dto.setOrgao(" ");
        dto.setDataAbertura(LocalDate.of(2026, 10, 1));
        dto.setDataProtocolo(LocalDate.of(2026, 10, 5));
    }

    private void salvarDevolvendoComId() {
        when(processoRepository.save(any(Processo.class))).thenAnswer(invocacao -> {
            Processo processo = invocacao.getArgument(0);
            processo.setId(20L);
            return processo;
        });
    }

    @Test
    void deveCriarProcessoComImovelDoCliente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(imovelRepository.findById(5L)).thenReturn(Optional.of(imovel));
        salvarDevolvendoComId();

        ProcessoResponseDTO resultado = processoService.criar(dto);

        assertEquals(20L, resultado.getId());
        assertEquals("Joao da Silva", resultado.getNomeCliente());
        assertEquals("Fazenda Boa Vista", resultado.getDenominacaoImovel());
        assertEquals("SIGEF-123", resultado.getNumeroProtocolo());
        assertNull(resultado.getOrgao());
    }

    @Test
    void deveCriarProcessoSemImovel() {
        dto.setImovelId(null);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        salvarDevolvendoComId();

        ProcessoResponseDTO resultado = processoService.criar(dto);

        assertNull(resultado.getImovelId());
        verify(imovelRepository, never()).findById(any());
    }

    @Test
    void naoDeveAceitarImovelDeOutroCliente() {
        Cliente outro = new Cliente();
        outro.setId(2L);
        imovel.setCliente(outro);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(imovelRepository.findById(5L)).thenReturn(Optional.of(imovel));

        assertThrows(BusinessException.class, () -> processoService.criar(dto));
        verify(processoRepository, never()).save(any(Processo.class));
    }

    @Test
    void naoDeveAceitarProtocoloAntesDaAbertura() {
        dto.setDataProtocolo(LocalDate.of(2026, 9, 30));

        assertThrows(BusinessException.class, () -> processoService.criar(dto));
        verify(processoRepository, never()).save(any(Processo.class));
    }

    @Test
    void naoDeveAceitarConclusaoAntesDaAbertura() {
        dto.setDataConclusao(LocalDate.of(2026, 9, 1));

        assertThrows(BusinessException.class, () -> processoService.criar(dto));
    }

    @Test
    void deveMarcarComoAtrasadoSomenteProcessoEmAndamentoComPrazoVencido() {
        Processo processo = new Processo();
        processo.setCliente(cliente);
        processo.setStatus(StatusProcesso.EM_ANALISE);
        processo.setPrazoPrevisto(LocalDate.now().minusDays(1));
        assertTrue(new ProcessoResponseDTO(processo).isAtrasado());

        processo.setStatus(StatusProcesso.DEFERIDO);
        assertFalse(new ProcessoResponseDTO(processo).isAtrasado());

        processo.setStatus(StatusProcesso.EXIGENCIA);
        processo.setPrazoPrevisto(LocalDate.now());
        assertFalse(new ProcessoResponseDTO(processo).isAtrasado());
    }

    @Test
    void deveListarRepassandoFiltros() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(processoRepository.buscar(1L, StatusProcesso.EXIGENCIA, "sigef")).thenReturn(List.of());

        processoService.listar(1L, StatusProcesso.EXIGENCIA, "  sigef ");

        verify(processoRepository).buscar(1L, StatusProcesso.EXIGENCIA, "sigef");
    }

    @Test
    void deveListarSemFiltros() {
        when(processoRepository.buscar(null, null, null)).thenReturn(List.of());

        processoService.listar(null, null, "  ");

        verify(processoRepository).buscar(null, null, null);
    }

    @Test
    void deveLancarExcecaoAoExcluirProcessoInexistente() {
        when(processoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> processoService.excluir(99L));
    }

    @Test
    void naoDeveExcluirProcessoComDocumentos() {
        Processo processo = new Processo();
        processo.setId(20L);
        when(processoRepository.findById(20L)).thenReturn(Optional.of(processo));
        when(documentoRepository.existsByProcessoId(20L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> processoService.excluir(20L));
        verify(processoRepository, never()).delete(any(Processo.class));
    }

    @Test
    void naoDeveExcluirProcessoComLancamentos() {
        Processo processo = new Processo();
        processo.setId(20L);
        when(processoRepository.findById(20L)).thenReturn(Optional.of(processo));
        when(lancamentoRepository.existsByProcessoId(20L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> processoService.excluir(20L));
        verify(processoRepository, never()).delete(any(Processo.class));
    }

    @Test
    void naoDeveTrocarClienteDeProcessoComDocumentos() {
        Cliente outro = new Cliente();
        outro.setId(2L);
        Processo processo = new Processo();
        processo.setId(20L);
        processo.setCliente(outro);
        when(processoRepository.findById(20L)).thenReturn(Optional.of(processo));
        when(documentoRepository.existsByProcessoId(20L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> processoService.atualizar(20L, dto));
        verify(processoRepository, never()).save(any(Processo.class));
    }
}
