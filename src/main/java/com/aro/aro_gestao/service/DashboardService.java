package com.aro.aro_gestao.service;

import com.aro.aro_gestao.dto.AtendimentoResponseDTO;
import com.aro.aro_gestao.dto.DashboardResponseDTO;
import com.aro.aro_gestao.dto.TarefaResponseDTO;
import com.aro.aro_gestao.entity.StatusCliente;
import com.aro.aro_gestao.entity.StatusTarefa;
import com.aro.aro_gestao.repository.AtendimentoRepository;
import com.aro.aro_gestao.repository.ClienteRepository;
import com.aro.aro_gestao.repository.TarefaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class DashboardService {

    private final ClienteRepository clienteRepository;
    private final TarefaRepository tarefaRepository;
    private final AtendimentoRepository atendimentoRepository;

    public DashboardService(ClienteRepository clienteRepository,
                            TarefaRepository tarefaRepository,
                            AtendimentoRepository atendimentoRepository) {
        this.clienteRepository = clienteRepository;
        this.tarefaRepository = tarefaRepository;
        this.atendimentoRepository = atendimentoRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponseDTO buscarResumo() {
        LocalDate hoje = LocalDate.now();

        DashboardResponseDTO resumo = new DashboardResponseDTO();
        resumo.setDataReferencia(hoje);
        resumo.setClientesAtivos(clienteRepository.countByStatus(StatusCliente.ATIVO));
        // Pendentes = nao concluidas com prazo de hoje em diante.
        // Atrasadas = nao concluidas com prazo anterior a hoje. As duas nunca se sobrepoem.
        resumo.setTarefasPendentes(tarefaRepository.countByStatusNotAndPrazoGreaterThanEqual(
                StatusTarefa.CONCLUIDA, hoje));
        resumo.setTarefasAtrasadas(tarefaRepository.countByStatusNotAndPrazoBefore(
                StatusTarefa.CONCLUIDA, hoje));

        resumo.setProximosPrazos(tarefaRepository
                .findTop5ByStatusNotAndPrazoGreaterThanEqualOrderByPrazoAscIdAsc(
                        StatusTarefa.CONCLUIDA, hoje)
                .stream()
                .map(TarefaResponseDTO::new)
                .toList());

        resumo.setUltimosAtendimentos(atendimentoRepository
                .findTop5ByOrderByDataHoraDescIdDesc()
                .stream()
                .map(AtendimentoResponseDTO::new)
                .toList());

        return resumo;
    }
}
