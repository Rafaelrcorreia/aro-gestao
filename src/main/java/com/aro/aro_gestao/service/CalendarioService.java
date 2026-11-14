package com.aro.aro_gestao.service;

import com.aro.aro_gestao.dto.CalendarioResponseDTO;
import com.aro.aro_gestao.dto.EventoCalendarioResponseDTO;
import com.aro.aro_gestao.entity.Atendimento;
import com.aro.aro_gestao.entity.Cliente;
import com.aro.aro_gestao.entity.Compromisso;
import com.aro.aro_gestao.entity.Processo;
import com.aro.aro_gestao.entity.Tarefa;
import com.aro.aro_gestao.exception.BusinessException;
import com.aro.aro_gestao.exception.ResourceNotFoundException;
import com.aro.aro_gestao.repository.AtendimentoRepository;
import com.aro.aro_gestao.repository.ClienteRepository;
import com.aro.aro_gestao.repository.CompromissoRepository;
import com.aro.aro_gestao.repository.ProcessoRepository;
import com.aro.aro_gestao.repository.TarefaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CalendarioService {
    private final TarefaRepository tarefaRepository;
    private final AtendimentoRepository atendimentoRepository;
    private final ClienteRepository clienteRepository;
    private final CompromissoRepository compromissoRepository;
    private final ProcessoRepository processoRepository;

    public CalendarioService(TarefaRepository tarefaRepository,
                             AtendimentoRepository atendimentoRepository,
                             ClienteRepository clienteRepository,
                             CompromissoRepository compromissoRepository,
                             ProcessoRepository processoRepository) {
        this.tarefaRepository = tarefaRepository;
        this.atendimentoRepository = atendimentoRepository;
        this.clienteRepository = clienteRepository;
        this.compromissoRepository = compromissoRepository;
        this.processoRepository = processoRepository;
    }

    @Transactional(readOnly = true)
    public CalendarioResponseDTO buscarMes(String mes, Long clienteId) {
        YearMonth periodo = validarMes(mes);
        if (clienteId != null) {
            if (clienteId <= 0) {
                throw new BusinessException("O clienteId deve ser um numero inteiro positivo.");
            }
            if (!clienteRepository.existsById(clienteId)) {
                throw new ResourceNotFoundException("Cliente nao encontrado.");
            }
        }

        List<Tarefa> tarefas;
        List<Atendimento> atendimentos;
        List<Compromisso> compromissos;
        List<Processo> processos;
        if (clienteId == null) {
            tarefas = tarefaRepository.findByPrazoBetweenOrderByPrazoAscIdAsc(
                    periodo.atDay(1), periodo.atEndOfMonth());
            atendimentos = atendimentoRepository.findByProximoContatoBetweenOrderByProximoContatoAscIdAsc(
                    periodo.atDay(1), periodo.atEndOfMonth());
            compromissos = compromissoRepository.findByDataBetweenOrderByDataAscIdAsc(
                    periodo.atDay(1), periodo.atEndOfMonth());
            processos = processoRepository.findByPrazoPrevistoBetweenOrderByPrazoPrevistoAscIdAsc(
                    periodo.atDay(1), periodo.atEndOfMonth());
        } else {
            tarefas = tarefaRepository.findByClienteIdAndPrazoBetweenOrderByPrazoAscIdAsc(
                    clienteId, periodo.atDay(1), periodo.atEndOfMonth());
            atendimentos = atendimentoRepository.findByClienteIdAndProximoContatoBetweenOrderByProximoContatoAscIdAsc(
                    clienteId, periodo.atDay(1), periodo.atEndOfMonth());
            compromissos = compromissoRepository.findByClienteIdAndDataBetweenOrderByDataAscIdAsc(
                    clienteId, periodo.atDay(1), periodo.atEndOfMonth());
            processos = processoRepository.findByClienteIdAndPrazoPrevistoBetweenOrderByPrazoPrevistoAscIdAsc(
                    clienteId, periodo.atDay(1), periodo.atEndOfMonth());
        }

        List<EventoCalendarioResponseDTO> eventos = new ArrayList<>();
        for (Tarefa tarefa : tarefas) {
            Cliente cliente = tarefa.getCliente();
            eventos.add(new EventoCalendarioResponseDTO(
                    "TAREFA", tarefa.getId(), tarefa.getPrazo(), tarefa.getTitulo(),
                    cliente.getId(), cliente.getNomeCompleto(), tarefa.getStatus(), tarefa.getPrioridade()));
        }
        for (Atendimento atendimento : atendimentos) {
            Cliente cliente = atendimento.getCliente();
            eventos.add(new EventoCalendarioResponseDTO(
                    "RETORNO", atendimento.getId(), atendimento.getProximoContato(), atendimento.getAssunto(),
                    cliente.getId(), cliente.getNomeCompleto(), null, null));
        }
        for (Compromisso compromisso : compromissos) {
            // O cliente do compromisso e opcional
            Cliente cliente = compromisso.getCliente();
            eventos.add(new EventoCalendarioResponseDTO(
                    "COMPROMISSO", compromisso.getId(), compromisso.getData(), compromisso.getHora(),
                    compromisso.getTitulo(),
                    cliente == null ? null : cliente.getId(),
                    cliente == null ? null : cliente.getNomeCompleto(),
                    null, null));
        }
        for (Processo processo : processos) {
            Cliente cliente = processo.getCliente();
            // Ex.: "Georreferenciamento - Fazenda Boa Vista"
            String titulo = processo.getTipo().getRotulo();
            if (processo.getImovel() != null) {
                titulo += " - " + processo.getImovel().getDenominacao();
            }
            eventos.add(new EventoCalendarioResponseDTO(
                    "PROCESSO", processo.getId(), processo.getPrazoPrevisto(), null, titulo,
                    cliente.getId(), cliente.getNomeCompleto(), null, null, processo.getStatus()));
        }
        eventos.sort(Comparator.comparing(EventoCalendarioResponseDTO::getData)
                .thenComparing(EventoCalendarioResponseDTO::getTipo)
                .thenComparing(EventoCalendarioResponseDTO::getOrigemId));
        return new CalendarioResponseDTO(periodo, eventos);
    }

    private YearMonth validarMes(String mes) {
        if (mes == null || !mes.matches("[1-9][0-9]{3}-(0[1-9]|1[0-2])")) {
            throw new BusinessException("Informe o mes no formato AAAA-MM, com ano entre 1000 e 9999.");
        }
        try {
            return YearMonth.parse(mes);
        } catch (DateTimeException ex) {
            throw new BusinessException("Mes invalido. Use o formato AAAA-MM.");
        }
    }
}

