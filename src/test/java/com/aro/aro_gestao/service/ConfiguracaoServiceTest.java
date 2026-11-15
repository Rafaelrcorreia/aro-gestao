package com.aro.aro_gestao.service;

import com.aro.aro_gestao.dto.ConfiguracaoDTO;
import com.aro.aro_gestao.entity.Configuracao;
import com.aro.aro_gestao.repository.ConfiguracaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfiguracaoServiceTest {

    @Mock
    private ConfiguracaoRepository configuracaoRepository;

    @InjectMocks
    private ConfiguracaoService configuracaoService;

    @Test
    void deveUsarValoresPadraoSemGravarQuandoNadaFoiSalvo() {
        when(configuracaoRepository.findById(Configuracao.ID_UNICO)).thenReturn(Optional.empty());

        ConfiguracaoDTO atual = configuracaoService.buscar();

        assertEquals("ARO Assessoria", atual.getNomeEmpresa());
        assertEquals(30, atual.getDiasAvisoDocumentos());
        assertEquals(3, atual.getDiasAlertaPrazo());
        assertNull(atual.getAtualizadoEm());
        verify(configuracaoRepository, never()).save(any());
    }

    @Test
    void deveSalvarNormalizandoERegistrandoQuemAlterou() {
        when(configuracaoRepository.findById(Configuracao.ID_UNICO)).thenReturn(Optional.empty());
        when(configuracaoRepository.save(any(Configuracao.class))).thenAnswer(inv -> inv.getArgument(0));
        ConfiguracaoDTO dto = new ConfiguracaoDTO();
        dto.setNomeEmpresa("  ARO Assessoria Rural  ");
        dto.setCnpj(" ");
        dto.setEstado("ba");
        dto.setDiasAvisoDocumentos(45);
        dto.setDiasAlertaPrazo(5);

        ConfiguracaoDTO salvo = configuracaoService.salvar(dto, "rafael");

        ArgumentCaptor<Configuracao> captor = ArgumentCaptor.forClass(Configuracao.class);
        verify(configuracaoRepository).save(captor.capture());
        // Sempre a mesma linha
        assertEquals(Configuracao.ID_UNICO, captor.getValue().getId());
        assertEquals("ARO Assessoria Rural", salvo.getNomeEmpresa());
        assertNull(salvo.getCnpj());
        assertEquals("BA", salvo.getEstado());
        assertEquals(45, salvo.getDiasAvisoDocumentos());
        assertEquals("rafael", salvo.getAtualizadoPor());
        assertNotNull(salvo.getAtualizadoEm());
    }

    @Test
    void deveLerDiasDeAvisoDaConfiguracaoSalva() {
        Configuracao salva = new Configuracao();
        salva.setDiasAvisoDocumentos(60);
        when(configuracaoRepository.findById(Configuracao.ID_UNICO)).thenReturn(Optional.of(salva));

        assertEquals(60, configuracaoService.diasAvisoDocumentos());
    }
}
