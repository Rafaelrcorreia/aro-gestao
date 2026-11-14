package com.aro.aro_gestao.service;

import com.aro.aro_gestao.dto.ItemChecklistRequestDTO;
import com.aro.aro_gestao.dto.ItemChecklistResponseDTO;
import com.aro.aro_gestao.dto.TarefaRequestDTO;
import com.aro.aro_gestao.dto.TarefaResponseDTO;
import com.aro.aro_gestao.entity.Cliente;
import com.aro.aro_gestao.entity.ItemChecklist;
import com.aro.aro_gestao.entity.Tarefa;
import com.aro.aro_gestao.exception.BusinessException;
import com.aro.aro_gestao.exception.ResourceNotFoundException;
import com.aro.aro_gestao.repository.ClienteRepository;
import com.aro.aro_gestao.repository.ItemChecklistRepository;
import com.aro.aro_gestao.repository.TarefaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final ClienteRepository clienteRepository;
    private final ItemChecklistRepository itemChecklistRepository;

    public TarefaService(TarefaRepository tarefaRepository, ClienteRepository clienteRepository,
                         ItemChecklistRepository itemChecklistRepository) {
        this.tarefaRepository = tarefaRepository;
        this.clienteRepository = clienteRepository;
        this.itemChecklistRepository = itemChecklistRepository;
    }

    public TarefaResponseDTO criar(TarefaRequestDTO dto) {
        Cliente cliente = buscarClienteOuFalhar(dto.getClienteId());

        Tarefa tarefa = new Tarefa();
        tarefa.setCliente(cliente);
        copiarDadosParaEntidade(dto, tarefa);

        Tarefa salva = tarefaRepository.save(tarefa);
        return new TarefaResponseDTO(salva);
    }

    public List<TarefaResponseDTO> listar(Long clienteId) {
        List<Tarefa> tarefas;

        if (clienteId != null) {
            buscarClienteOuFalhar(clienteId);
            tarefas = tarefaRepository.findByClienteIdOrderByPrazoAsc(clienteId);
        } else {
            tarefas = tarefaRepository.findAllByOrderByPrazoAsc();
        }

        return tarefas.stream().map(TarefaResponseDTO::new).toList();
    }

    public TarefaResponseDTO buscarPorId(Long id) {
        Tarefa tarefa = buscarTarefaOuFalhar(id);
        return new TarefaResponseDTO(tarefa);
    }

    public TarefaResponseDTO atualizar(Long id, TarefaRequestDTO dto) {
        Tarefa tarefa = buscarTarefaOuFalhar(id);
        Cliente cliente = buscarClienteOuFalhar(dto.getClienteId());

        tarefa.setCliente(cliente);
        copiarDadosParaEntidade(dto, tarefa);

        Tarefa atualizada = tarefaRepository.save(tarefa);
        return new TarefaResponseDTO(atualizada);
    }

    public void excluir(Long id) {
        Tarefa tarefa = buscarTarefaOuFalhar(id);
        tarefaRepository.delete(tarefa);
    }

    public ItemChecklistResponseDTO adicionarItemChecklist(Long tarefaId, ItemChecklistRequestDTO dto) {
        Tarefa tarefa = buscarTarefaOuFalhar(tarefaId);

        ItemChecklist item = new ItemChecklist();
        item.setTarefa(tarefa);
        item.setDescricao(dto.getDescricao());
        item.setConcluido(false);

        ItemChecklist salvo = itemChecklistRepository.save(item);
        return new ItemChecklistResponseDTO(salvo);
    }

    public ItemChecklistResponseDTO alternarConclusaoItem(Long tarefaId, Long itemId) {
        ItemChecklist item = buscarItemDaTarefaOuFalhar(tarefaId, itemId);
        item.setConcluido(!item.isConcluido());

        ItemChecklist atualizado = itemChecklistRepository.save(item);
        return new ItemChecklistResponseDTO(atualizado);
    }

    public void removerItemChecklist(Long tarefaId, Long itemId) {
        ItemChecklist item = buscarItemDaTarefaOuFalhar(tarefaId, itemId);
        itemChecklistRepository.delete(item);
    }

    private ItemChecklist buscarItemDaTarefaOuFalhar(Long tarefaId, Long itemId) {
        buscarTarefaOuFalhar(tarefaId);

        ItemChecklist item = itemChecklistRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Item de checklist com id " + itemId + " nao encontrado"));

        if (!item.getTarefa().getId().equals(tarefaId)) {
            throw new BusinessException("Este item de checklist nao pertence a tarefa informada");
        }

        return item;
    }

    private Tarefa buscarTarefaOuFalhar(Long id) {
        return tarefaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tarefa com id " + id + " nao encontrada"));
    }

    private Cliente buscarClienteOuFalhar(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente com id " + clienteId + " nao encontrado"));
    }

    private void copiarDadosParaEntidade(TarefaRequestDTO dto, Tarefa tarefa) {
        tarefa.setTitulo(dto.getTitulo());
        tarefa.setDescricao(dto.getDescricao());
        tarefa.setResponsavel(dto.getResponsavel());
        tarefa.setPrioridade(dto.getPrioridade());
        tarefa.setPrazo(dto.getPrazo());
        tarefa.setStatus(dto.getStatus());
    }
}