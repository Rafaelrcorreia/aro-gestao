package com.aro.aro_gestao.service;

import com.aro.aro_gestao.dto.LancamentoRequestDTO;
import com.aro.aro_gestao.dto.LancamentoResponseDTO;
import com.aro.aro_gestao.dto.PagamentoRequestDTO;
import com.aro.aro_gestao.dto.ResumoFinanceiroResponseDTO;
import com.aro.aro_gestao.entity.Cliente;
import com.aro.aro_gestao.entity.FormaPagamento;
import com.aro.aro_gestao.entity.Lancamento;
import com.aro.aro_gestao.entity.Processo;
import com.aro.aro_gestao.entity.TipoLancamento;
import com.aro.aro_gestao.exception.BusinessException;
import com.aro.aro_gestao.exception.ResourceNotFoundException;
import com.aro.aro_gestao.repository.ClienteRepository;
import com.aro.aro_gestao.repository.LancamentoRepository;
import com.aro.aro_gestao.repository.ProcessoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class LancamentoService {

    private final LancamentoRepository lancamentoRepository;
    private final ClienteRepository clienteRepository;
    private final ProcessoRepository processoRepository;

    public LancamentoService(LancamentoRepository lancamentoRepository,
                             ClienteRepository clienteRepository,
                             ProcessoRepository processoRepository) {
        this.lancamentoRepository = lancamentoRepository;
        this.clienteRepository = clienteRepository;
        this.processoRepository = processoRepository;
    }

    @Transactional
    public LancamentoResponseDTO criar(LancamentoRequestDTO dto) {
        Lancamento lancamento = new Lancamento();
        copiarDadosParaEntidade(dto, lancamento);
        return new LancamentoResponseDTO(lancamentoRepository.save(lancamento));
    }

    @Transactional(readOnly = true)
    public List<LancamentoResponseDTO> listar(String mes, TipoLancamento tipo, Long clienteId, String busca) {
        YearMonth periodo = validarMes(mes);
        if (clienteId != null) {
            buscarClienteOuFalhar(clienteId);
        }
        String termo = (busca == null || busca.isBlank()) ? null : busca.trim();

        return lancamentoRepository.buscarNoPeriodo(periodo.atDay(1), periodo.atEndOfMonth(), tipo, clienteId, termo)
                .stream()
                .map(LancamentoResponseDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResumoFinanceiroResponseDTO resumir(String mes) {
        YearMonth periodo = validarMes(mes);
        LocalDate inicio = periodo.atDay(1);
        LocalDate fim = periodo.atEndOfMonth();
        LocalDate hoje = LocalDate.now();

        return new ResumoFinanceiroResponseDTO(
                periodo.toString(), inicio, fim,
                lancamentoRepository.somarPagosNoPeriodo(TipoLancamento.RECEITA, inicio, fim),
                lancamentoRepository.somarPagosNoPeriodo(TipoLancamento.DESPESA, inicio, fim),
                lancamentoRepository.somarEmAbertoNoPeriodo(TipoLancamento.RECEITA, inicio, fim),
                lancamentoRepository.somarEmAbertoNoPeriodo(TipoLancamento.DESPESA, inicio, fim),
                lancamentoRepository.somarAtrasados(TipoLancamento.RECEITA, hoje),
                lancamentoRepository.somarAtrasados(TipoLancamento.DESPESA, hoje));
    }

    @Transactional(readOnly = true)
    public LancamentoResponseDTO buscarPorId(Long id) {
        return new LancamentoResponseDTO(buscarLancamentoOuFalhar(id));
    }

    @Transactional
    public LancamentoResponseDTO atualizar(Long id, LancamentoRequestDTO dto) {
        Lancamento lancamento = buscarLancamentoOuFalhar(id);
        copiarDadosParaEntidade(dto, lancamento);
        return new LancamentoResponseDTO(lancamentoRepository.save(lancamento));
    }

    @Transactional
    public LancamentoResponseDTO registrarPagamento(Long id, PagamentoRequestDTO dto) {
        Lancamento lancamento = buscarLancamentoOuFalhar(id);
        validarDataPagamento(dto.getDataPagamento());
        lancamento.setDataPagamento(dto.getDataPagamento());
        lancamento.setFormaPagamento(dto.getFormaPagamento());
        return new LancamentoResponseDTO(lancamentoRepository.save(lancamento));
    }

    // Volta o lancamento para "em aberto" (ex.: pagamento marcado por engano)
    @Transactional
    public LancamentoResponseDTO desfazerPagamento(Long id) {
        Lancamento lancamento = buscarLancamentoOuFalhar(id);
        lancamento.setDataPagamento(null);
        lancamento.setFormaPagamento(null);
        return new LancamentoResponseDTO(lancamentoRepository.save(lancamento));
    }

    @Transactional
    public void excluir(Long id) {
        lancamentoRepository.delete(buscarLancamentoOuFalhar(id));
    }

    private Lancamento buscarLancamentoOuFalhar(Long id) {
        return lancamentoRepository.findComVinculosById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Lancamento com id " + id + " nao encontrado"));
    }

    private Cliente buscarClienteOuFalhar(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente com id " + clienteId + " nao encontrado"));
    }

    private Processo buscarProcessoDoCliente(Long processoId, Cliente cliente) {
        Processo processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Processo com id " + processoId + " nao encontrado"));
        if (!processo.getCliente().getId().equals(cliente.getId())) {
            throw new BusinessException("O processo informado nao pertence ao cliente selecionado.");
        }
        return processo;
    }

    // Pagamento e um fato: nao pode estar no futuro
    private void validarDataPagamento(LocalDate dataPagamento) {
        if (dataPagamento != null && dataPagamento.isAfter(LocalDate.now())) {
            throw new BusinessException("A data de pagamento nao pode ser no futuro.");
        }
    }

    private void copiarDadosParaEntidade(LancamentoRequestDTO dto, Lancamento lancamento) {
        validarDataPagamento(dto.getDataPagamento());
        if (dto.getProcessoId() != null && dto.getClienteId() == null) {
            throw new BusinessException("Para vincular um processo, informe tambem o cliente.");
        }
        Cliente cliente = dto.getClienteId() == null ? null : buscarClienteOuFalhar(dto.getClienteId());

        lancamento.setTipo(dto.getTipo());
        lancamento.setCategoria(dto.getCategoria());
        lancamento.setDescricao(dto.getDescricao().trim());
        lancamento.setValor(dto.getValor());
        lancamento.setDataVencimento(dto.getDataVencimento());
        lancamento.setDataPagamento(dto.getDataPagamento());
        // Forma de pagamento so faz sentido quando ja foi pago
        FormaPagamento forma = dto.getDataPagamento() == null ? null : dto.getFormaPagamento();
        lancamento.setFormaPagamento(forma);
        lancamento.setCliente(cliente);
        lancamento.setProcesso(dto.getProcessoId() == null ? null : buscarProcessoDoCliente(dto.getProcessoId(), cliente));
        lancamento.setObservacoes(textoOuNulo(dto.getObservacoes()));
    }

    private YearMonth validarMes(String mes) {
        if (mes == null || !mes.matches("[1-9][0-9]{3}-(0[1-9]|1[0-2])")) {
            throw new BusinessException("Informe o mes no formato AAAA-MM.");
        }
        try {
            return YearMonth.parse(mes);
        } catch (DateTimeException ex) {
            throw new BusinessException("Mes invalido. Use o formato AAAA-MM.");
        }
    }

    // Campos opcionais em branco viram null no banco
    private String textoOuNulo(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
