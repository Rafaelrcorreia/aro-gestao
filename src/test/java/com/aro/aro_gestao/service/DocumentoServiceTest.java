package com.aro.aro_gestao.service;

import com.aro.aro_gestao.dto.DocumentoRequestDTO;
import com.aro.aro_gestao.dto.DocumentoResponseDTO;
import com.aro.aro_gestao.dto.DocumentoResponseDTO.SituacaoValidade;
import com.aro.aro_gestao.entity.Cliente;
import com.aro.aro_gestao.entity.Documento;
import com.aro.aro_gestao.entity.Imovel;
import com.aro.aro_gestao.entity.Processo;
import com.aro.aro_gestao.entity.TipoDocumento;
import com.aro.aro_gestao.entity.TipoProcesso;
import com.aro.aro_gestao.exception.BusinessException;
import com.aro.aro_gestao.repository.ClienteRepository;
import com.aro.aro_gestao.repository.DocumentoRepository;
import com.aro.aro_gestao.repository.ImovelRepository;
import com.aro.aro_gestao.repository.ProcessoRepository;
import com.aro.aro_gestao.service.ArmazenamentoDocumentosService.ArquivoSalvo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentoServiceTest {

    @Mock
    private DocumentoRepository documentoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ImovelRepository imovelRepository;

    @Mock
    private ProcessoRepository processoRepository;

    @Mock
    private ArmazenamentoDocumentosService armazenamento;

    @InjectMocks
    private DocumentoService documentoService;

    private Cliente cliente;
    private DocumentoRequestDTO dto;
    private MultipartFile pdf;

    @BeforeEach
    void configurar() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNomeCompleto("Joao da Silva");

        dto = new DocumentoRequestDTO();
        dto.setClienteId(1L);
        dto.setTipo(TipoDocumento.CERTIDAO);
        dto.setTitulo("  Certidao de inteiro teor  ");
        dto.setNumero(" ");
        dto.setDataEmissao(LocalDate.of(2026, 10, 1));
        dto.setDataValidade(LocalDate.of(2026, 10, 31));

        pdf = new MockMultipartFile("arquivo", "certidao.pdf", "application/pdf", new byte[]{1, 2, 3});
    }

    private void salvarDevolvendoComId() {
        when(documentoRepository.save(any(Documento.class))).thenAnswer(invocacao -> {
            Documento documento = invocacao.getArgument(0);
            documento.setId(30L);
            return documento;
        });
    }

    @Test
    void deveCriarDocumentoComArquivo() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(armazenamento.salvar(pdf)).thenReturn(new ArquivoSalvo("uuid.pdf", "certidao.pdf", 3L));
        salvarDevolvendoComId();

        DocumentoResponseDTO resultado = documentoService.criar(dto, pdf);

        assertEquals(30L, resultado.getId());
        assertEquals("Certidao de inteiro teor", resultado.getTitulo());
        assertEquals(null, resultado.getNumero());
        assertTrue(resultado.isPossuiArquivo());
        assertEquals("certidao.pdf", resultado.getArquivoNome());
        assertEquals(LocalDate.now(), resultado.getDataCadastro());
    }

    @Test
    void deveCriarDocumentoSemArquivo() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        salvarDevolvendoComId();

        DocumentoResponseDTO resultado = documentoService.criar(dto, null);

        assertFalse(resultado.isPossuiArquivo());
        verify(armazenamento, never()).salvar(any());
    }

    @Test
    void deveApagarArquivoNovoSeOBancoFalhar() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(armazenamento.salvar(pdf)).thenReturn(new ArquivoSalvo("uuid.pdf", "certidao.pdf", 3L));
        when(documentoRepository.save(any(Documento.class))).thenThrow(new IllegalStateException("banco fora"));

        assertThrows(IllegalStateException.class, () -> documentoService.criar(dto, pdf));
        verify(armazenamento).excluir("uuid.pdf");
    }

    @Test
    void naoDeveGravarArquivoQuandoOsDadosSaoInvalidos() {
        dto.setDataValidade(LocalDate.of(2026, 9, 1));

        assertThrows(BusinessException.class, () -> documentoService.criar(dto, pdf));
        verify(armazenamento, never()).salvar(any());
    }

    @Test
    void naoDeveAceitarImovelOuProcessoDeOutroCliente() {
        Cliente outro = new Cliente();
        outro.setId(2L);
        Imovel imovel = new Imovel();
        imovel.setCliente(outro);
        Processo processo = new Processo();
        processo.setCliente(outro);
        processo.setTipo(TipoProcesso.CAR);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(imovelRepository.findById(5L)).thenReturn(Optional.of(imovel));
        when(processoRepository.findById(6L)).thenReturn(Optional.of(processo));

        dto.setImovelId(5L);
        assertThrows(BusinessException.class, () -> documentoService.criar(dto, null));

        dto.setImovelId(null);
        dto.setProcessoId(6L);
        assertThrows(BusinessException.class, () -> documentoService.criar(dto, null));
        verify(documentoRepository, never()).save(any(Documento.class));
    }

    @Test
    void deveSubstituirArquivoEApagarOAntigoSoDepoisDeSalvar() {
        Documento existente = new Documento();
        existente.setId(30L);
        existente.setCliente(cliente);
        existente.setArquivoNomeArmazenado("antigo.pdf");
        when(documentoRepository.findComVinculosById(30L)).thenReturn(Optional.of(existente));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(armazenamento.salvar(pdf)).thenReturn(new ArquivoSalvo("novo.pdf", "certidao.pdf", 3L));
        when(documentoRepository.save(any(Documento.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        DocumentoResponseDTO resultado = documentoService.atualizar(30L, dto, pdf);

        assertEquals("certidao.pdf", resultado.getArquivoNome());
        verify(armazenamento).excluir("antigo.pdf");
        verify(armazenamento, never()).excluir("novo.pdf");
    }

    @Test
    void deveManterArquivoAtualQuandoNaoEnviaOutro() {
        Documento existente = new Documento();
        existente.setId(30L);
        existente.setCliente(cliente);
        existente.setArquivoNomeArmazenado("atual.pdf");
        existente.setArquivoNomeOriginal("atual.pdf");
        when(documentoRepository.findComVinculosById(30L)).thenReturn(Optional.of(existente));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(documentoRepository.save(any(Documento.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        DocumentoResponseDTO resultado = documentoService.atualizar(30L, dto, null);

        assertTrue(resultado.isPossuiArquivo());
        verify(armazenamento, never()).excluir(any());
    }

    @Test
    void deveApagarArquivoAoExcluirDocumento() {
        Documento existente = new Documento();
        existente.setId(30L);
        existente.setArquivoNomeArmazenado("atual.pdf");
        when(documentoRepository.findComVinculosById(30L)).thenReturn(Optional.of(existente));

        documentoService.excluir(30L);

        verify(documentoRepository).delete(existente);
        verify(armazenamento).excluir("atual.pdf");
    }

    @Test
    void deveCalcularSituacaoDaValidade() {
        LocalDate hoje = LocalDate.of(2026, 11, 14);
        assertEquals(SituacaoValidade.SEM_VALIDADE, DocumentoResponseDTO.calcularSituacao(null, hoje));
        assertEquals(SituacaoValidade.VENCIDO, DocumentoResponseDTO.calcularSituacao(hoje.minusDays(1), hoje));
        assertEquals(SituacaoValidade.VENCENDO, DocumentoResponseDTO.calcularSituacao(hoje, hoje));
        assertEquals(SituacaoValidade.VENCENDO, DocumentoResponseDTO.calcularSituacao(hoje.plusDays(30), hoje));
        assertEquals(SituacaoValidade.VALIDO, DocumentoResponseDTO.calcularSituacao(hoje.plusDays(31), hoje));
    }
}
