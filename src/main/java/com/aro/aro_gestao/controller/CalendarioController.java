package com.aro.aro_gestao.controller;

import com.aro.aro_gestao.dto.CalendarioResponseDTO;
import com.aro.aro_gestao.exception.BusinessException;
import com.aro.aro_gestao.service.CalendarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calendario")
public class CalendarioController {
    private final CalendarioService calendarioService;

    public CalendarioController(CalendarioService calendarioService) {
        this.calendarioService = calendarioService;
    }

    @GetMapping
    public ResponseEntity<CalendarioResponseDTO> buscarMes(
            @RequestParam(name = "mes", required = false) String mes,
            @RequestParam(name = "clienteId", required = false) String clienteId) {
        return ResponseEntity.ok(calendarioService.buscarMes(mes, converterClienteId(clienteId)));
    }

    private Long converterClienteId(String valor) {
        if (valor == null) {
            return null;
        }
        if (!valor.matches("[0-9]+")) {
            throw new BusinessException("O clienteId deve ser um numero inteiro positivo.");
        }
        try {
            long id = Long.parseLong(valor);
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException ex) {
            throw new BusinessException("O clienteId deve ser um numero inteiro positivo valido.");
        }
    }
}

