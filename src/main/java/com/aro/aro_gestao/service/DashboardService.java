package com.aro.aro_gestao.service;

import com.aro.aro_gestao.dto.AtendimentoResponseDTO;
import com.aro.aro_gestao.dto.DashboardResponseDTO;
import com.aro.aro_gestao.dto.TarefaResponseDTO;
import com.aro.aro_gestao.entity.StatusCliente;
import com.aro.aro_gestao.entity.StatusProcesso;
import com.aro.aro_gestao.entity.StatusTarefa;
import com.aro.aro_gestao.entity.TipoLancamento;
import com.aro.aro_gestao.repository.AtendimentoRepository;
import com.aro.aro_gestao.repository.ClienteRepository;
import com.aro.aro_gestao.repository.DocumentoRepository;
import com.aro.aro_gestao.repository.LancamentoRepository;
import com.aro.aro_gestao.repository.ProcessoRepository;
import com.aro.aro_gestao.repository.TarefaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class DashboardService {

    private final ClienteRepository clienteRepository;
    private final TarefaRepository tarefaRepository;
    private final AtendimentoRepository atendimentoRepository;
    private final ProcessoRepository processoRepository;
    private final DocumentoRepository documentoRepository;
    private final LancamentoRepository lancamentoRepository;
    private final ConfiguracaoService configuracaoService;

    public DashboardService(ClienteRepository clienteRepository,
                            TarefaRepository tarefaRepository,
                            AtendimentoRepository atendimentoRepository,
                            ProcessoRepository processoRepository,
                            DocumentoRepository documentoRepository,
                            LancamentoRepository lancamentoRepository,
                            ConfiguracaoService configuracaoService) {
        this.clienteRepository = clienteRepository;
        this.tarefaRepository = tarefaRepository;
        this.atendimentoRepository = atendimentoRepository;
        this.processoRepository = processoRepository;
        this.documentoRepository = documentoRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.configuracaoService = configuracaoService;
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

        // Em andamento = nao deferido, indeferido nem arquivado.
        // Atrasados = em andamento com prazo previsto antes de hoje (subconjunto dos em andamento).
        resumo.setProcessosEmAndamento(processoRepository.countByStatusNotIn(StatusProcesso.FINALIZADOS));
        resumo.setProcessosAtrasados(processoRepository.countByStatusNotInAndPrazoPrevistoBefore(
                StatusProcesso.FINALIZADOS, hoje));

        // Mesma regra da lista de documentos: vencido = validade antes de hoje;
        // vencendo = validade de hoje ate daqui a N dias (N nas Configuracoes). As duas nunca se sobrepoem.
        resumo.setDocumentosVencidos(documentoRepository.countByDataValidadeBefore(hoje));
        resumo.setDocumentosVencendo(documentoRepository.countByDataValidadeBetween(
                hoje, hoje.plusDays(configuracaoService.diasAvisoDocumentos())));

        // Receitas ainda nao recebidas com vencimento antes de hoje (de qualquer mes)
        resumo.setReceberEmAtraso(lancamentoRepository.somarAtrasados(TipoLancamento.RECEITA, hoje));

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
