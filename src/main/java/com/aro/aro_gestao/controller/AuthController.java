package com.aro.aro_gestao.controller;

import com.aro.aro_gestao.dto.AlterarSenhaRequestDTO;
import com.aro.aro_gestao.dto.PrimeiroAcessoRequestDTO;
import com.aro.aro_gestao.dto.UsuarioResponseDTO;
import com.aro.aro_gestao.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// O login (POST /api/auth/login) e o logout (POST /api/auth/logout) sao tratados pelo Spring Security
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Publico. Tambem garante o cookie XSRF-TOKEN antes da tela de login enviar o formulario.
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status(CsrfToken csrfToken) {
        csrfToken.getToken();
        return ResponseEntity.ok(Map.of("configurado", usuarioService.existeAlgumUsuario()));
    }

    // Publico, mas so funciona enquanto nao existe nenhum usuario
    @PostMapping("/primeiro-acesso")
    public ResponseEntity<UsuarioResponseDTO> primeiroAcesso(@Valid @RequestBody PrimeiroAcessoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criarPrimeiroAdministrador(dto));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> usuarioLogado(Authentication autenticacao, CsrfToken csrfToken) {
        csrfToken.getToken();
        return ResponseEntity.ok(usuarioService.buscarPorLogin(autenticacao.getName()));
    }

    @PutMapping("/senha")
    public ResponseEntity<Void> alterarSenha(Authentication autenticacao,
                                             @Valid @RequestBody AlterarSenhaRequestDTO dto) {
        usuarioService.alterarPropriaSenha(autenticacao.getName(), dto);
        return ResponseEntity.noContent().build();
    }
}
