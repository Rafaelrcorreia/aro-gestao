package com.aro.aro_gestao.service;

import com.aro.aro_gestao.dto.ConfiguracaoDTO;
import com.aro.aro_gestao.entity.Configuracao;
import com.aro.aro_gestao.repository.ConfiguracaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class ConfiguracaoService {

    private final ConfiguracaoRepository configuracaoRepository;

    public ConfiguracaoService(ConfiguracaoRepository configuracaoRepository) {
        this.configuracaoRepository = configuracaoRepository;
    }

    // Enquanto ninguem salvou, vale a configuracao padrao (sem gravar nada no banco)
    @Transactional(readOnly = true)
    public Configuracao obter() {
        return configuracaoRepository.findById(Configuracao.ID_UNICO).orElseGet(Configuracao::new);
    }

    public int diasAvisoDocumentos() {
        return obter().getDiasAvisoDocumentos();
    }

    @Transactional(readOnly = true)
    public ConfiguracaoDTO buscar() {
        return new ConfiguracaoDTO(obter());
    }

    @Transactional
    public ConfiguracaoDTO salvar(ConfiguracaoDTO dto, String loginDeQuemSalva) {
        Configuracao configuracao = obter();
        configuracao.setId(Configuracao.ID_UNICO);
        configuracao.setNomeEmpresa(dto.getNomeEmpresa().trim());
        configuracao.setCnpj(textoOuNulo(dto.getCnpj()));
        configuracao.setTelefone(textoOuNulo(dto.getTelefone()));
        configuracao.setEmail(textoOuNulo(dto.getEmail()));
        configuracao.setEndereco(textoOuNulo(dto.getEndereco()));
        configuracao.setCidade(textoOuNulo(dto.getCidade()));
        String estado = textoOuNulo(dto.getEstado());
        configuracao.setEstado(estado == null ? null : estado.toUpperCase(Locale.ROOT));
        configuracao.setDiasAvisoDocumentos(dto.getDiasAvisoDocumentos());
        configuracao.setDiasAlertaPrazo(dto.getDiasAlertaPrazo());
        configuracao.setAtualizadoEm(LocalDateTime.now());
        configuracao.setAtualizadoPor(loginDeQuemSalva);
        return new ConfiguracaoDTO(configuracaoRepository.save(configuracao));
    }

    private static String textoOuNulo(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
