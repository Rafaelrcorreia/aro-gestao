package com.aro.aro_gestao.controller;

import com.aro.aro_gestao.dto.LancamentoRequestDTO;
import com.aro.aro_gestao.dto.LancamentoResponseDTO;
import com.aro.aro_gestao.dto.PagamentoRequestDTO;
import com.aro.aro_gestao.dto.ResumoFinanceiroResponseDTO;
import com.aro.aro_gestao.entity.TipoLancamento;
import com.aro.aro_gestao.service.LancamentoService;
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
@RequestMapping("/api/lancamentos")
public class LancamentoController {

    private final LancamentoService lancamentoService;

    public LancamentoController(LancamentoService lancamentoService) {
        this.lancamentoService = lancamentoService;
    }

    @PostMapping
    public ResponseEntity<LancamentoResponseDTO> criar(@Valid @RequestBody LancamentoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lancamentoService.criar(dto));
    }

    // mes no formato AAAA-MM (obrigatorio); demais filtros opcionais
    @GetMapping
    public ResponseEntity<List<LancamentoResponseDTO>> listar(
            @RequestParam(name = "mes", required = false) String mes,
            @RequestParam(name = "tipo", required = false) TipoLancamento tipo,
            @RequestParam(name = "clienteId", required = false) Long clienteId,
            @RequestParam(name = "busca", required = false) String busca) {

        return ResponseEntity.ok(lancamentoService.listar(mes, tipo, clienteId, busca));
    }

    @GetMapping("/resumo")
    public ResponseEntity<ResumoFinanceiroResponseDTO> resumir(
            @RequestParam(name = "mes", required = false) String mes) {
        return ResponseEntity.ok(lancamentoService.resumir(mes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LancamentoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(lancamentoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LancamentoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody LancamentoRequestDTO dto) {

        return ResponseEntity.ok(lancamentoService.atualizar(id, dto));
    }

    @PatchMapping("/{id}/pagamento")
    public ResponseEntity<LancamentoResponseDTO> registrarPagamento(
            @PathVariable Long id,
            @Valid @RequestBody PagamentoRequestDTO dto) {

        return ResponseEntity.ok(lancamentoService.registrarPagamento(id, dto));
    }

    @DeleteMapping("/{id}/pagamento")
    public ResponseEntity<LancamentoResponseDTO> desfazerPagamento(@PathVariable Long id) {
        return ResponseEntity.ok(lancamentoService.desfazerPagamento(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        lancamentoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
