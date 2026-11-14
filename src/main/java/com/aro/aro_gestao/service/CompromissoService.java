package com.aro.aro_gestao.service;

import com.aro.aro_gestao.dto.CompromissoRequestDTO;
import com.aro.aro_gestao.dto.CompromissoResponseDTO;
import com.aro.aro_gestao.entity.Cliente;
import com.aro.aro_gestao.entity.Compromisso;
import com.aro.aro_gestao.exception.ResourceNotFoundException;
import com.aro.aro_gestao.repository.ClienteRepository;
import com.aro.aro_gestao.repository.CompromissoRepository;
import org.springframework.stereotype.Service;

@Service
public class CompromissoService {

    private final CompromissoRepository compromissoRepository;
    private final ClienteRepository clienteRepository;

    public CompromissoService(CompromissoRepository compromissoRepository,
                              ClienteRepository clienteRepository) {
        this.compromissoRepository = compromissoRepository;
        this.clienteRepository = clienteRepository;
    }

    public CompromissoResponseDTO criar(CompromissoRequestDTO dto) {
        Compromisso compromisso = new Compromisso();
        copiarDadosParaEntidade(dto, compromisso);

        Compromisso salvo = compromissoRepository.save(compromisso);
        return new CompromissoResponseDTO(salvo);
    }

    public CompromissoResponseDTO buscarPorId(Long id) {
        Compromisso compromisso = buscarCompromissoOuFalhar(id);
        return new CompromissoResponseDTO(compromisso);
    }

    public CompromissoResponseDTO atualizar(Long id, CompromissoRequestDTO dto) {
        Compromisso compromisso = buscarCompromissoOuFalhar(id);
        copiarDadosParaEntidade(dto, compromisso);

        Compromisso atualizado = compromissoRepository.save(compromisso);
        return new CompromissoResponseDTO(atualizado);
    }

    public void excluir(Long id) {
        Compromisso compromisso = buscarCompromissoOuFalhar(id);
        compromissoRepository.delete(compromisso);
    }

    private Compromisso buscarCompromissoOuFalhar(Long id) {
        return compromissoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Compromisso com id " + id + " nao encontrado"));
    }

    private Cliente buscarClienteOuFalhar(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente com id " + clienteId + " nao encontrado"));
    }

    private void copiarDadosParaEntidade(CompromissoRequestDTO dto, Compromisso compromisso) {
        // Cliente opcional: sem clienteId, o compromisso fica sem vinculo
        if (dto.getClienteId() == null) {
            compromisso.setCliente(null);
        } else {
            compromisso.setCliente(buscarClienteOuFalhar(dto.getClienteId()));
        }

        compromisso.setTitulo(dto.getTitulo().trim());
        compromisso.setData(dto.getData());
        compromisso.setHora(dto.getHora());

        String descricao = dto.getDescricao();
        if (descricao == null || descricao.isBlank()) {
            compromisso.setDescricao(null);
        } else {
            compromisso.setDescricao(descricao.trim());
        }
    }
}
