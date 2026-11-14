package com.aro.aro_gestao.controller;

import com.aro.aro_gestao.dto.ImovelRequestDTO;
import com.aro.aro_gestao.dto.ImovelResponseDTO;
import com.aro.aro_gestao.service.ImovelService;
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
@RequestMapping("/api/imoveis")
public class ImovelController {

    private final ImovelService imovelService;

    public ImovelController(ImovelService imovelService) {
        this.imovelService = imovelService;
    }

    @PostMapping
    public ResponseEntity<ImovelResponseDTO> criar(@Valid @RequestBody ImovelRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(imovelService.criar(dto));
    }

    @GetMapping
    public ResponseEntity<List<ImovelResponseDTO>> listar(
            @RequestParam(name = "clienteId", required = false) Long clienteId,
            @RequestParam(name = "busca", required = false) String busca) {

        return ResponseEntity.ok(imovelService.listar(clienteId, busca));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImovelResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(imovelService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ImovelResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ImovelRequestDTO dto) {

        return ResponseEntity.ok(imovelService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        imovelService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
