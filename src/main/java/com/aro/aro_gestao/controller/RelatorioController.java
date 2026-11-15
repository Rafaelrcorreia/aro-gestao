package com.aro.aro_gestao.controller;

import com.aro.aro_gestao.dto.RelatorioDTOs.RelatorioClientes;
import com.aro.aro_gestao.dto.RelatorioDTOs.RelatorioDocumentos;
import com.aro.aro_gestao.dto.RelatorioDTOs.RelatorioFinanceiro;
import com.aro.aro_gestao.dto.RelatorioDTOs.RelatorioProcessos;
import com.aro.aro_gestao.service.RelatorioService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    // Datas no formato AAAA-MM-DD
    @GetMapping("/financeiro")
    public ResponseEntity<RelatorioFinanceiro> financeiro(
            @RequestParam(name = "inicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(name = "fim", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(relatorioService.financeiro(inicio, fim));
    }

    @GetMapping("/processos")
    public ResponseEntity<RelatorioProcessos> processos() {
        return ResponseEntity.ok(relatorioService.processos());
    }

    // dias = janela de vencimento a partir de hoje (padrao 30); os ja vencidos sempre entram
    @GetMapping("/documentos")
    public ResponseEntity<RelatorioDocumentos> documentos(
            @RequestParam(name = "dias", required = false) Integer dias) {
        return ResponseEntity.ok(relatorioService.documentos(dias));
    }

    @GetMapping("/clientes")
    public ResponseEntity<RelatorioClientes> clientes() {
        return ResponseEntity.ok(relatorioService.clientes());
    }
}
