package com.aro.aro_gestao.service;

import com.aro.aro_gestao.dto.DocumentoResponseDTO;
import com.aro.aro_gestao.dto.LancamentoResponseDTO;
import com.aro.aro_gestao.dto.ProcessoResponseDTO;
import com.aro.aro_gestao.dto.RelatorioDTOs.QuantidadePorStatus;
import com.aro.aro_gestao.dto.RelatorioDTOs.QuantidadePorTipo;
import com.aro.aro_gestao.dto.RelatorioDTOs.RecebidoPorCliente;
import com.aro.aro_gestao.dto.RelatorioDTOs.RelatorioClientes;
import com.aro.aro_gestao.dto.RelatorioDTOs.RelatorioDocumentos;
import com.aro.aro_gestao.dto.RelatorioDTOs.RelatorioFinanceiro;
import com.aro.aro_gestao.dto.RelatorioDTOs.RelatorioProcessos;
import com.aro.aro_gestao.dto.RelatorioDTOs.ResumoCliente;
import com.aro.aro_gestao.dto.RelatorioDTOs.TotalPorCategoria;
import com.aro.aro_gestao.entity.Cliente;
import com.aro.aro_gestao.entity.Lancamento;
import com.aro.aro_gestao.entity.Processo;
import com.aro.aro_gestao.entity.StatusProcesso;
import com.aro.aro_gestao.entity.TipoLancamento;
import com.aro.aro_gestao.entity.TipoProcesso;
import com.aro.aro_gestao.exception.BusinessException;
import com.aro.aro_gestao.repository.ClienteRepository;
import com.aro.aro_gestao.repository.DocumentoRepository;
import com.aro.aro_gestao.repository.ImovelRepository;
import com.aro.aro_gestao.repository.LancamentoRepository;
import com.aro.aro_gestao.repository.ProcessoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class RelatorioService {

    // Periodo maximo do relatorio financeiro (evita consultas enormes por engano)
    static final long DIAS_MAXIMOS_PERIODO = 3660;

    private final LancamentoRepository lancamentoRepository;
    private final ProcessoRepository processoRepository;
    private final DocumentoRepository documentoRepository;
    private final ClienteRepository clienteRepository;
    private final ImovelRepository imovelRepository;
    private final ConfiguracaoService configuracaoService;

    public RelatorioService(LancamentoRepository lancamentoRepository,
                            ProcessoRepository processoRepository,
                            DocumentoRepository documentoRepository,
                            ClienteRepository clienteRepository,
                            ImovelRepository imovelRepository,
                            ConfiguracaoService configuracaoService) {
        this.lancamentoRepository = lancamentoRepository;
        this.processoRepository = processoRepository;
        this.documentoRepository = documentoRepository;
        this.clienteRepository = clienteRepository;
        this.imovelRepository = imovelRepository;
        this.configuracaoService = configuracaoService;
    }

    /* ---------- Financeiro ---------- */

    public RelatorioFinanceiro financeiro(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            throw new BusinessException("Informe a data inicial e a data final do periodo.");
        }
        if (fim.isBefore(inicio)) {
            throw new BusinessException("A data final nao pode ser anterior a data inicial.");
        }
        if (ChronoUnit.DAYS.between(inicio, fim) > DIAS_MAXIMOS_PERIODO) {
            throw new BusinessException("O periodo pode ter no maximo 10 anos.");
        }

        List<Lancamento> pagos = lancamentoRepository.buscarPagosNoPeriodo(inicio, fim);

        BigDecimal recebido = BigDecimal.ZERO;
        BigDecimal pago = BigDecimal.ZERO;
        // Chave: tipo + categoria, na ordem em que aparecem
        Map<String, TotalPorCategoria> porCategoria = new LinkedHashMap<>();
        Map<Long, RecebidoPorCliente> porCliente = new HashMap<>();

        for (Lancamento lancamento : pagos) {
            BigDecimal valor = lancamento.getValor();
            boolean receita = lancamento.getTipo() == TipoLancamento.RECEITA;
            if (receita) {
                recebido = recebido.add(valor);
            } else {
                pago = pago.add(valor);
            }

            String chave = lancamento.getTipo() + ":" + lancamento.getCategoria();
            TotalPorCategoria atual = porCategoria.get(chave);
            porCategoria.put(chave, atual == null
                    ? new TotalPorCategoria(lancamento.getTipo(), lancamento.getCategoria(), 1, valor)
                    : new TotalPorCategoria(atual.tipo(), atual.categoria(), atual.quantidade() + 1, atual.total().add(valor)));

            if (receita) {
                Cliente cliente = lancamento.getCliente();
                Long idCliente = cliente == null ? null : cliente.getId();
                RecebidoPorCliente doCliente = porCliente.get(idCliente);
                porCliente.put(idCliente, doCliente == null
                        ? new RecebidoPorCliente(idCliente, cliente == null ? null : cliente.getNomeCompleto(), 1, valor)
                        : new RecebidoPorCliente(idCliente, doCliente.nomeCliente(), doCliente.quantidade() + 1,
                        doCliente.total().add(valor)));
            }
        }

        // Receitas primeiro, e dentro de cada tipo do maior para o menor valor
        List<TotalPorCategoria> categorias = new ArrayList<>(porCategoria.values());
        categorias.sort(Comparator.comparing(TotalPorCategoria::tipo)
                .thenComparing(TotalPorCategoria::total, Comparator.reverseOrder()));
        List<RecebidoPorCliente> clientes = new ArrayList<>(porCliente.values());
        clientes.sort(Comparator.comparing(RecebidoPorCliente::total, Comparator.reverseOrder()));

        return new RelatorioFinanceiro(inicio, fim, recebido, pago, recebido.subtract(pago),
                lancamentoRepository.somarEmAbertoNoPeriodo(TipoLancamento.RECEITA, inicio, fim),
                lancamentoRepository.somarEmAbertoNoPeriodo(TipoLancamento.DESPESA, inicio, fim),
                categorias, clientes,
                pagos.stream().map(LancamentoResponseDTO::new).toList());
    }

    /* ---------- Processos ---------- */

    public RelatorioProcessos processos() {
        List<Processo> todos = processoRepository.buscarTodosComVinculos();

        Map<StatusProcesso, Long> porStatus = new EnumMap<>(StatusProcesso.class);
        for (StatusProcesso status : StatusProcesso.values()) {
            porStatus.put(status, 0L);
        }
        Map<TipoProcesso, long[]> porTipo = new EnumMap<>(TipoProcesso.class);
        List<ProcessoResponseDTO> emAndamento = new ArrayList<>();
        long comPrazoVencido = 0;

        for (Processo processo : todos) {
            porStatus.merge(processo.getStatus(), 1L, Long::sum);
            long[] contagem = porTipo.computeIfAbsent(processo.getTipo(), tipo -> new long[2]);
            contagem[0]++;
            if (!processo.getStatus().isFinalizado()) {
                contagem[1]++;
                ProcessoResponseDTO dto = new ProcessoResponseDTO(processo);
                emAndamento.add(dto);
                if (dto.isAtrasado()) {
                    comPrazoVencido++;
                }
            }
        }

        // Em andamento: prazo mais proximo primeiro; sem prazo vai para o fim
        emAndamento.sort(Comparator.comparing(ProcessoResponseDTO::getPrazoPrevisto,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(ProcessoResponseDTO::getDataAbertura));

        List<QuantidadePorStatus> status = porStatus.entrySet().stream()
                .map(e -> new QuantidadePorStatus(e.getKey(), e.getValue())).toList();
        List<QuantidadePorTipo> tipos = porTipo.entrySet().stream()
                .map(e -> new QuantidadePorTipo(e.getKey(), e.getValue()[0], e.getValue()[1]))
                .sorted(Comparator.comparing(QuantidadePorTipo::total, Comparator.reverseOrder()))
                .toList();

        return new RelatorioProcessos(todos.size(), emAndamento.size(), comPrazoVencido, status, tipos, emAndamento);
    }

    /* ---------- Documentos ---------- */

    public RelatorioDocumentos documentos(Integer dias) {
        int diasAviso = configuracaoService.diasAvisoDocumentos();
        int janela = dias == null ? diasAviso : dias;
        if (janela < 0 || janela > 365) {
            throw new BusinessException("Informe de 0 a 365 dias.");
        }
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(janela);

        List<DocumentoResponseDTO> documentos = documentoRepository
                .findByDataValidadeLessThanEqualOrderByDataValidadeAscIdAsc(limite)
                .stream().map(documento -> new DocumentoResponseDTO(documento, diasAviso)).toList();
        long vencidos = documentos.stream().filter(d -> d.getDataValidade().isBefore(hoje)).count();

        return new RelatorioDocumentos(hoje, janela, limite, vencidos, documentos.size() - vencidos, documentos);
    }

    /* ---------- Carteira de clientes ---------- */

    public RelatorioClientes clientes() {
        LocalDate hoje = LocalDate.now();
        Map<Long, Long> imoveis = paraMapaDeContagem(imovelRepository.contarPorCliente());
        Map<Long, Long> processos = paraMapaDeContagem(
                processoRepository.contarEmAndamentoPorCliente(StatusProcesso.FINALIZADOS));
        Map<Long, Long> documentos = paraMapaDeContagem(documentoRepository.contarVencidosPorCliente(hoje));
        Map<Long, BigDecimal> atrasos = new HashMap<>();
        for (Object[] linha : lancamentoRepository.somarReceberEmAtrasoPorCliente(hoje)) {
            atrasos.put((Long) linha[0], (BigDecimal) linha[1]);
        }

        List<ResumoCliente> linhas = new ArrayList<>();
        long totalImoveis = 0;
        long totalProcessos = 0;
        long totalDocumentos = 0;
        BigDecimal totalAtraso = BigDecimal.ZERO;

        for (Cliente cliente : clienteRepository.findAll()) {
            Long id = cliente.getId();
            long qtdImoveis = imoveis.getOrDefault(id, 0L);
            long qtdProcessos = processos.getOrDefault(id, 0L);
            long qtdDocumentos = documentos.getOrDefault(id, 0L);
            BigDecimal atraso = atrasos.getOrDefault(id, BigDecimal.ZERO);

            String cidadeUf = juntarCidadeUf(cliente.getCidade(), cliente.getEstado());
            linhas.add(new ResumoCliente(id, cliente.getNomeCompleto(), cliente.getStatus(), cidadeUf,
                    qtdImoveis, qtdProcessos, qtdDocumentos, atraso));

            totalImoveis += qtdImoveis;
            totalProcessos += qtdProcessos;
            totalDocumentos += qtdDocumentos;
            totalAtraso = totalAtraso.add(atraso);
        }
        linhas.sort(Comparator.comparing(ResumoCliente::nomeCliente, String.CASE_INSENSITIVE_ORDER));

        return new RelatorioClientes(linhas.size(), totalImoveis, totalProcessos, totalDocumentos, totalAtraso, linhas);
    }

    private static Map<Long, Long> paraMapaDeContagem(List<Object[]> linhas) {
        Map<Long, Long> mapa = new HashMap<>();
        for (Object[] linha : linhas) {
            mapa.put((Long) linha[0], ((Number) linha[1]).longValue());
        }
        return mapa;
    }

    private static String juntarCidadeUf(String cidade, String uf) {
        boolean temCidade = cidade != null && !cidade.isBlank();
        boolean temUf = uf != null && !uf.isBlank();
        if (temCidade && temUf) {
            return cidade + "/" + uf;
        }
        return temCidade ? cidade : (temUf ? uf : null);
    }
}
