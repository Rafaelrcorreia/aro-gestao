package com.aro.aro_gestao.controller;

import com.aro.aro_gestao.dto.CompromissoRequestDTO;
import com.aro.aro_gestao.dto.CompromissoResponseDTO;
import com.aro.aro_gestao.service.CompromissoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/compromissos")
public class CompromissoController {

    private final CompromissoService compromissoService;

    public CompromissoController(CompromissoService compromissoService) {
        this.compromissoService = compromissoService;
    }

    @PostMapping
    public ResponseEntity<CompromissoResponseDTO> criar(@Valid @RequestBody CompromissoRequestDTO dto) {
        CompromissoResponseDTO criado = compromissoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompromissoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(compromissoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompromissoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CompromissoRequestDTO dto) {

        return ResponseEntity.ok(compromissoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        compromissoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
