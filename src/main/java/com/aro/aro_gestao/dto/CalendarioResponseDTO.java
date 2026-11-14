package com.aro.aro_gestao.dto;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class CalendarioResponseDTO {
    private final String mes;
    private final LocalDate inicio;
    private final LocalDate fim;
    private final List<EventoCalendarioResponseDTO> eventos;

    public CalendarioResponseDTO(YearMonth mes, List<EventoCalendarioResponseDTO> eventos) {
        this.mes = mes.toString();
        this.inicio = mes.atDay(1);
        this.fim = mes.atEndOfMonth();
        this.eventos = List.copyOf(eventos);
    }

    public String getMes() { return mes; }
    public LocalDate getInicio() { return inicio; }
    public LocalDate getFim() { return fim; }
    public List<EventoCalendarioResponseDTO> getEventos() { return eventos; }
}

