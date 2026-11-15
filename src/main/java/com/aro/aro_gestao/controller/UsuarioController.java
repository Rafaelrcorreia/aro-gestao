package com.aro.aro_gestao.controller;

import com.aro.aro_gestao.dto.AlterarSenhaRequestDTO;
import com.aro.aro_gestao.dto.UsuarioRequestDTO;
import com.aro.aro_gestao.dto.UsuarioResponseDTO;
import com.aro.aro_gestao.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Somente administradores (regra em SecurityConfig)
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(@Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> atualizar(@PathVariable Long id,
                                                        @Valid @RequestBody UsuarioRequestDTO dto,
                                                        Authentication autenticacao) {
        return ResponseEntity.ok(usuarioService.atualizar(id, dto, autenticacao.getName()));
    }

    @PutMapping("/{id}/senha")
    public ResponseEntity<Void> redefinirSenha(@PathVariable Long id,
                                               @Valid @RequestBody AlterarSenhaRequestDTO dto,
                                               Authentication autenticacao) {
        usuarioService.redefinirSenha(id, dto, autenticacao.getName());
        return ResponseEntity.noContent().build();
    }
}
