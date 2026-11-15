package com.aro.aro_gestao.controller;

import com.aro.aro_gestao.dto.ConfiguracaoDTO;
import com.aro.aro_gestao.service.ConfiguracaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Qualquer usuario logado le; so administradores alteram (regra em SecurityConfig)
@RestController
@RequestMapping("/api/configuracoes")
public class ConfiguracaoController {

    private final ConfiguracaoService configuracaoService;

    public ConfiguracaoController(ConfiguracaoService configuracaoService) {
        this.configuracaoService = configuracaoService;
    }

    @GetMapping
    public ResponseEntity<ConfiguracaoDTO> buscar() {
        return ResponseEntity.ok(configuracaoService.buscar());
    }

    @PutMapping
    public ResponseEntity<ConfiguracaoDTO> salvar(@Valid @RequestBody ConfiguracaoDTO dto, Authentication autenticacao) {
        return ResponseEntity.ok(configuracaoService.salvar(dto, autenticacao.getName()));
    }
}
