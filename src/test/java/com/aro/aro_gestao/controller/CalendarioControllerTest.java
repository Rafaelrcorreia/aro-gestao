package com.aro.aro_gestao.controller;

import com.aro.aro_gestao.dto.CalendarioResponseDTO;
import com.aro.aro_gestao.exception.BusinessException;
import com.aro.aro_gestao.exception.GlobalExceptionHandler;
import com.aro.aro_gestao.exception.ResourceNotFoundException;
import com.aro.aro_gestao.service.CalendarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.YearMonth;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CalendarioControllerTest {
    private CalendarioService service;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        service = mock(CalendarioService.class);
        mvc = MockMvcBuilders.standaloneSetup(new CalendarioController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void deveResponderJsonComMesVazio() throws Exception {
        when(service.buscarMes("2026-10", null)).thenReturn(
                new CalendarioResponseDTO(YearMonth.of(2026, 10), List.of()));
        mvc.perform(get("/api/calendario").param("mes", "2026-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mes").value("2026-10"))
                .andExpect(jsonPath("$.eventos").isEmpty());
    }

    @Test
    void deveEncaminharFiltroDeCliente() throws Exception {
        when(service.buscarMes("2026-10", 6L)).thenReturn(
                new CalendarioResponseDTO(YearMonth.of(2026, 10), List.of()));
        mvc.perform(get("/api/calendario").param("mes", "2026-10").param("clienteId", "6"))
                .andExpect(status().isOk());
        verify(service).buscarMes("2026-10", 6L);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "abc", "0", "-1", "1.5", "9223372036854775808"})
    void deveResponder400ParaClienteIdInvalido(String id) throws Exception {
        mvc.perform(get("/api/calendario").param("mes", "2026-10").param("clienteId", id))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void deveResponder400QuandoMesNaoInformado() throws Exception {
        when(service.buscarMes(null, null)).thenThrow(new BusinessException("Informe o mes."));
        mvc.perform(get("/api/calendario")).andExpect(status().isBadRequest());
    }

    @Test
    void deveResponder404ParaClienteInexistente() throws Exception {
        when(service.buscarMes("2026-10", 999L)).thenThrow(new ResourceNotFoundException("Cliente nao encontrado."));
        mvc.perform(get("/api/calendario").param("mes", "2026-10").param("clienteId", "999"))
                .andExpect(status().isNotFound());
    }
}

