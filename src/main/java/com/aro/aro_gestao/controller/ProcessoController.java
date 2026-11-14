package com.aro.aro_gestao.controller;

import com.aro.aro_gestao.dto.ProcessoRequestDTO;
import com.aro.aro_gestao.dto.ProcessoResponseDTO;
import com.aro.aro_gestao.entity.StatusProcesso;
import com.aro.aro_gestao.service.ProcessoService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/processos")
public class ProcessoController {

    private final ProcessoService processoService;

    public ProcessoController(ProcessoService processoService) {
        this.processoService = processoService;
    }

    @PostMapping
    public ResponseEntity<ProcessoResponseDTO> criar(@Valid @RequestBody ProcessoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(processoService.criar(dto));
    }

    @GetMapping
    public ResponseEntity<List<ProcessoResponseDTO>> listar(
            @RequestParam(name = "clienteId", required = false) Long clienteId,
            @RequestParam(name = "status", required = false) StatusProcesso status,
            @RequestParam(name = "busca", required = false) String busca) {

        return ResponseEntity.ok(processoService.listar(clienteId, status, busca));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcessoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(processoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProcessoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProcessoRequestDTO dto) {

        return ResponseEntity.ok(processoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        processoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
