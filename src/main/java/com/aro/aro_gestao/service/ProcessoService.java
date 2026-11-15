package com.aro.aro_gestao.service;

import com.aro.aro_gestao.dto.ProcessoRequestDTO;
import com.aro.aro_gestao.dto.ProcessoResponseDTO;
import com.aro.aro_gestao.entity.Cliente;
import com.aro.aro_gestao.entity.Imovel;
import com.aro.aro_gestao.entity.Processo;
import com.aro.aro_gestao.entity.StatusProcesso;
import com.aro.aro_gestao.exception.BusinessException;
import com.aro.aro_gestao.exception.ResourceNotFoundException;
import com.aro.aro_gestao.repository.ClienteRepository;
import com.aro.aro_gestao.repository.DocumentoRepository;
import com.aro.aro_gestao.repository.ImovelRepository;
import com.aro.aro_gestao.repository.LancamentoRepository;
import com.aro.aro_gestao.repository.ProcessoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProcessoService {

    private final ProcessoRepository processoRepository;
    private final ClienteRepository clienteRepository;
    private final ImovelRepository imovelRepository;
    private final DocumentoRepository documentoRepository;
    private final LancamentoRepository lancamentoRepository;

    public ProcessoService(ProcessoRepository processoRepository,
                           ClienteRepository clienteRepository,
                           ImovelRepository imovelRepository,
                           DocumentoRepository documentoRepository,
                           LancamentoRepository lancamentoRepository) {
        this.processoRepository = processoRepository;
        this.clienteRepository = clienteRepository;
        this.imovelRepository = imovelRepository;
        this.documentoRepository = documentoRepository;
        this.lancamentoRepository = lancamentoRepository;
    }

    @Transactional
    public ProcessoResponseDTO criar(ProcessoRequestDTO dto) {
        Processo processo = new Processo();
        copiarDadosParaEntidade(dto, processo);
        return new ProcessoResponseDTO(processoRepository.save(processo));
    }

    @Transactional(readOnly = true)
    public List<ProcessoResponseDTO> listar(Long clienteId, StatusProcesso status, String busca) {
        if (clienteId != null) {
            buscarClienteOuFalhar(clienteId);
        }
        String termo = (busca == null || busca.isBlank()) ? null : busca.trim();

        return processoRepository.buscar(clienteId, status, termo)
                .stream()
                .map(ProcessoResponseDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProcessoResponseDTO buscarPorId(Long id) {
        return new ProcessoResponseDTO(buscarProcessoOuFalhar(id));
    }

    @Transactional
    public ProcessoResponseDTO atualizar(Long id, ProcessoRequestDTO dto) {
        Processo processo = buscarProcessoOuFalhar(id);
        boolean trocouCliente = !processo.getCliente().getId().equals(dto.getClienteId());
        if (trocouCliente && (documentoRepository.existsByProcessoId(id)
                || lancamentoRepository.existsByProcessoId(id))) {
            // Os documentos/lancamentos do processo ficariam ligados a outro cliente
            throw new BusinessException("Nao e possivel trocar o cliente deste processo porque ele possui "
                    + "documentos ou lancamentos financeiros vinculados ao cliente atual.");
        }
        copiarDadosParaEntidade(dto, processo);
        return new ProcessoResponseDTO(processoRepository.save(processo));
    }

    @Transactional
    public void excluir(Long id) {
        Processo processo = buscarProcessoOuFalhar(id);
        if (documentoRepository.existsByProcessoId(id) || lancamentoRepository.existsByProcessoId(id)) {
            throw new BusinessException("Nao e possivel excluir o processo porque ele possui documentos ou "
                    + "lancamentos financeiros vinculados. Exclua esses registros ou retire o processo deles antes.");
        }
        processoRepository.delete(processo);
    }

    private Processo buscarProcessoOuFalhar(Long id) {
        return processoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Processo com id " + id + " nao encontrado"));
    }

    private Cliente buscarClienteOuFalhar(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente com id " + clienteId + " nao encontrado"));
    }

    private Imovel buscarImovelDoCliente(Long imovelId, Cliente cliente) {
        Imovel imovel = imovelRepository.findById(imovelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Imovel com id " + imovelId + " nao encontrado"));
        if (!imovel.getCliente().getId().equals(cliente.getId())) {
            throw new BusinessException("O imovel informado nao pertence ao cliente selecionado.");
        }
        return imovel;
    }

    private void validarDatas(ProcessoRequestDTO dto) {
        LocalDate abertura = dto.getDataAbertura();
        if (dto.getDataProtocolo() != null && dto.getDataProtocolo().isBefore(abertura)) {
            throw new BusinessException("A data de protocolo nao pode ser anterior a data de abertura.");
        }
        if (dto.getDataConclusao() != null && dto.getDataConclusao().isBefore(abertura)) {
            throw new BusinessException("A data de conclusao nao pode ser anterior a data de abertura.");
        }
    }

    private void copiarDadosParaEntidade(ProcessoRequestDTO dto, Processo processo) {
        validarDatas(dto);
        Cliente cliente = buscarClienteOuFalhar(dto.getClienteId());

        processo.setCliente(cliente);
        processo.setImovel(dto.getImovelId() == null ? null : buscarImovelDoCliente(dto.getImovelId(), cliente));
        processo.setTipo(dto.getTipo());
        processo.setStatus(dto.getStatus());
        processo.setNumeroProtocolo(textoOuNulo(dto.getNumeroProtocolo()));
        processo.setOrgao(textoOuNulo(dto.getOrgao()));
        processo.setDataAbertura(dto.getDataAbertura());
        processo.setDataProtocolo(dto.getDataProtocolo());
        processo.setPrazoPrevisto(dto.getPrazoPrevisto());
        processo.setDataConclusao(dto.getDataConclusao());
        processo.setDescricao(textoOuNulo(dto.getDescricao()));
    }

    // Campos opcionais em branco viram null no banco
    private String textoOuNulo(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
