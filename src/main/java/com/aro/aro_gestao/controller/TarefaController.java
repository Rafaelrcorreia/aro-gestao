package com.aro.aro_gestao.controller;

import com.aro.aro_gestao.dto.ItemChecklistRequestDTO;
import com.aro.aro_gestao.dto.ItemChecklistResponseDTO;
import com.aro.aro_gestao.dto.TarefaRequestDTO;
import com.aro.aro_gestao.dto.TarefaResponseDTO;
import com.aro.aro_gestao.service.TarefaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tarefas")
public class TarefaController {

    private final TarefaService tarefaService;

    public TarefaController(TarefaService tarefaService) {
        this.tarefaService = tarefaService;
    }

    @PostMapping
    public ResponseEntity<TarefaResponseDTO> criar(@Valid @RequestBody TarefaRequestDTO dto) {
        TarefaResponseDTO criada = tarefaService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @GetMapping
    public ResponseEntity<List<TarefaResponseDTO>> listar(
            @RequestParam(name = "clienteId", required = false) Long clienteId) {

        return ResponseEntity.ok(tarefaService.listar(clienteId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TarefaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tarefaService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TarefaResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody TarefaRequestDTO dto) {

        return ResponseEntity.ok(tarefaService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        tarefaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{tarefaId}/checklist")
    public ResponseEntity<ItemChecklistResponseDTO> adicionarItemChecklist(
            @PathVariable Long tarefaId,
            @Valid @RequestBody ItemChecklistRequestDTO dto) {

        ItemChecklistResponseDTO criado = tarefaService.adicionarItemChecklist(tarefaId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PatchMapping("/{tarefaId}/checklist/{itemId}")
    public ResponseEntity<ItemChecklistResponseDTO> alternarConclusaoItem(
            @PathVariable Long tarefaId,
            @PathVariable Long itemId) {

        return ResponseEntity.ok(tarefaService.alternarConclusaoItem(tarefaId, itemId));
    }

    @DeleteMapping("/{tarefaId}/checklist/{itemId}")
    public ResponseEntity<Void> removerItemChecklist(
            @PathVariable Long tarefaId,
            @PathVariable Long itemId) {

        tarefaService.removerItemChecklist(tarefaId, itemId);
        return ResponseEntity.noContent().build();
    }
}
