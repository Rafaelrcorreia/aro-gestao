package com.aro.aro_gestao.service;

import com.aro.aro_gestao.dto.AtendimentoRequestDTO;
import com.aro.aro_gestao.dto.AtendimentoResponseDTO;
import com.aro.aro_gestao.entity.Atendimento;
import com.aro.aro_gestao.entity.Cliente;
import com.aro.aro_gestao.exception.ResourceNotFoundException;
import com.aro.aro_gestao.repository.AtendimentoRepository;
import com.aro.aro_gestao.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AtendimentoService {

    private final AtendimentoRepository atendimentoRepository;
    private final ClienteRepository clienteRepository;

    public AtendimentoService(AtendimentoRepository atendimentoRepository, ClienteRepository clienteRepository) {
        this.atendimentoRepository = atendimentoRepository;
        this.clienteRepository = clienteRepository;
    }

    public AtendimentoResponseDTO criar(AtendimentoRequestDTO dto) {
        Cliente cliente = buscarClienteOuFalhar(dto.getClienteId());

        Atendimento atendimento = new Atendimento();
        atendimento.setCliente(cliente);
        copiarDadosParaEntidade(dto, atendimento);

        Atendimento salvo = atendimentoRepository.save(atendimento);
        return new AtendimentoResponseDTO(salvo);
    }

    public List<AtendimentoResponseDTO> listarPorCliente(Long clienteId) {
        buscarClienteOuFalhar(clienteId);

        return atendimentoRepository.findByClienteIdOrderByDataHoraDesc(clienteId)
                .stream()
                .map(AtendimentoResponseDTO::new)
                .toList();
    }

    public AtendimentoResponseDTO buscarPorId(Long id) {
        Atendimento atendimento = buscarAtendimentoOuFalhar(id);
        return new AtendimentoResponseDTO(atendimento);
    }

    public AtendimentoResponseDTO atualizar(Long id, AtendimentoRequestDTO dto) {
        Atendimento atendimento = buscarAtendimentoOuFalhar(id);
        Cliente cliente = buscarClienteOuFalhar(dto.getClienteId());

        atendimento.setCliente(cliente);
        copiarDadosParaEntidade(dto, atendimento);

        Atendimento atualizado = atendimentoRepository.save(atendimento);
        return new AtendimentoResponseDTO(atualizado);
    }

    public void excluir(Long id) {
        Atendimento atendimento = buscarAtendimentoOuFalhar(id);
        atendimentoRepository.delete(atendimento);
    }

    private Atendimento buscarAtendimentoOuFalhar(Long id) {
        return atendimentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Atendimento com id " + id + " nao encontrado"));
    }

    private Cliente buscarClienteOuFalhar(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente com id " + clienteId + " nao encontrado"));
    }

    private void copiarDadosParaEntidade(AtendimentoRequestDTO dto, Atendimento atendimento) {
        atendimento.setDataHora(dto.getDataHora());
        atendimento.setCanal(dto.getCanal());
        atendimento.setAssunto(dto.getAssunto());
        atendimento.setDescricao(dto.getDescricao());
        atendimento.setProximoContato(dto.getProximoContato());
    }
}