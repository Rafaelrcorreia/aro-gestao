package com.aro.aro_gestao.service;

import com.aro.aro_gestao.dto.ClienteRequestDTO;
import com.aro.aro_gestao.dto.ClienteResponseDTO;
import com.aro.aro_gestao.entity.Cliente;
import com.aro.aro_gestao.exception.BusinessException;
import com.aro.aro_gestao.exception.ResourceNotFoundException;
import com.aro.aro_gestao.repository.AtendimentoRepository;
import com.aro.aro_gestao.repository.ClienteRepository;
import com.aro.aro_gestao.repository.CompromissoRepository;
import com.aro.aro_gestao.repository.TarefaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final AtendimentoRepository atendimentoRepository;
    private final TarefaRepository tarefaRepository;
    private final CompromissoRepository compromissoRepository;

    public ClienteService(ClienteRepository clienteRepository,
                          AtendimentoRepository atendimentoRepository,
                          TarefaRepository tarefaRepository,
                          CompromissoRepository compromissoRepository) {
        this.clienteRepository = clienteRepository;
        this.atendimentoRepository = atendimentoRepository;
        this.tarefaRepository = tarefaRepository;
        this.compromissoRepository = compromissoRepository;
    }

    public ClienteResponseDTO criar(ClienteRequestDTO dto) {
        validarCpfCnpjDuplicado(dto.getCpfCnpj(), null);

        Cliente cliente = new Cliente();
        copiarDadosParaEntidade(dto, cliente);
        cliente.setDataCadastro(LocalDate.now());

        Cliente salvo = clienteRepository.save(cliente);
        return new ClienteResponseDTO(salvo);
    }

    public List<ClienteResponseDTO> listarTodos() {
        return clienteRepository.findAll()
                .stream()
                .map(ClienteResponseDTO::new)
                .toList();
    }

    public ClienteResponseDTO buscarPorId(Long id) {
        Cliente cliente = buscarClienteOuFalhar(id);
        return new ClienteResponseDTO(cliente);
    }

    public List<ClienteResponseDTO> buscarPorTexto(String busca) {
        return clienteRepository.buscarPorTexto(busca)
                .stream()
                .map(ClienteResponseDTO::new)
                .toList();
    }

    public ClienteResponseDTO atualizar(Long id, ClienteRequestDTO dto) {
        Cliente cliente = buscarClienteOuFalhar(id);
        validarCpfCnpjDuplicado(dto.getCpfCnpj(), id);

        copiarDadosParaEntidade(dto, cliente);

        Cliente atualizado = clienteRepository.save(cliente);
        return new ClienteResponseDTO(atualizado);
    }

    public void excluir(Long id) {
        Cliente cliente = buscarClienteOuFalhar(id);
        validarSemVinculos(id);
        clienteRepository.delete(cliente);
    }

    // Sem esta checagem o banco recusaria a exclusao (chave estrangeira)
    // e o usuario receberia apenas um erro interno generico
    private void validarSemVinculos(Long clienteId) {
        List<String> vinculos = new ArrayList<>();
        if (atendimentoRepository.existsByClienteId(clienteId)) {
            vinculos.add("atendimentos");
        }
        if (tarefaRepository.existsByClienteId(clienteId)) {
            vinculos.add("tarefas");
        }
        if (compromissoRepository.existsByClienteId(clienteId)) {
            vinculos.add("compromissos");
        }
        if (!vinculos.isEmpty()) {
            throw new BusinessException("Nao e possivel excluir o cliente porque ele possui "
                    + String.join(", ", vinculos)
                    + " vinculados. Exclua esses registros ou marque o cliente como inativo.");
        }
    }

    private Cliente buscarClienteOuFalhar(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente com id " + id + " nao encontrado"));
    }

    private void validarCpfCnpjDuplicado(String cpfCnpj, Long idAtual) {
        Optional<Cliente> existente = clienteRepository.findByCpfCnpj(cpfCnpj);

        if (existente.isPresent() && !existente.get().getId().equals(idAtual)) {
            throw new BusinessException("Ja existe um cliente cadastrado com este CPF/CNPJ");
        }
    }

    private void copiarDadosParaEntidade(ClienteRequestDTO dto, Cliente cliente) {
        cliente.setNomeCompleto(dto.getNomeCompleto());
        cliente.setTipoPessoa(dto.getTipoPessoa());
        cliente.setCpfCnpj(dto.getCpfCnpj());
        cliente.setTelefone(dto.getTelefone());
        cliente.setWhatsapp(dto.getWhatsapp());
        cliente.setEmail(dto.getEmail());
        cliente.setEndereco(dto.getEndereco());
        cliente.setCidade(dto.getCidade());
        cliente.setEstado(dto.getEstado());
        cliente.setObservacoes(dto.getObservacoes());
        cliente.setStatus(dto.getStatus());
    }
}
