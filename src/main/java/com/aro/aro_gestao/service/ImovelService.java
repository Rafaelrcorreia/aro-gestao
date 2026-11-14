package com.aro.aro_gestao.service;

import com.aro.aro_gestao.dto.ImovelRequestDTO;
import com.aro.aro_gestao.dto.ImovelResponseDTO;
import com.aro.aro_gestao.entity.Cliente;
import com.aro.aro_gestao.entity.Imovel;
import com.aro.aro_gestao.exception.BusinessException;
import com.aro.aro_gestao.exception.ResourceNotFoundException;
import com.aro.aro_gestao.repository.ClienteRepository;
import com.aro.aro_gestao.repository.DocumentoRepository;
import com.aro.aro_gestao.repository.ImovelRepository;
import com.aro.aro_gestao.repository.ProcessoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ImovelService {

    private final ImovelRepository imovelRepository;
    private final ClienteRepository clienteRepository;
    private final ProcessoRepository processoRepository;
    private final DocumentoRepository documentoRepository;

    public ImovelService(ImovelRepository imovelRepository, ClienteRepository clienteRepository,
                         ProcessoRepository processoRepository, DocumentoRepository documentoRepository) {
        this.imovelRepository = imovelRepository;
        this.clienteRepository = clienteRepository;
        this.processoRepository = processoRepository;
        this.documentoRepository = documentoRepository;
    }

    @Transactional
    public ImovelResponseDTO criar(ImovelRequestDTO dto) {
        Imovel imovel = new Imovel();
        copiarDadosParaEntidade(dto, imovel);
        return new ImovelResponseDTO(imovelRepository.save(imovel));
    }

    @Transactional(readOnly = true)
    public List<ImovelResponseDTO> listar(Long clienteId, String busca) {
        if (clienteId != null) {
            buscarClienteOuFalhar(clienteId);
        }
        String termo = (busca == null || busca.isBlank()) ? null : busca.trim();

        return imovelRepository.buscar(clienteId, termo)
                .stream()
                .map(ImovelResponseDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public ImovelResponseDTO buscarPorId(Long id) {
        return new ImovelResponseDTO(buscarImovelOuFalhar(id));
    }

    @Transactional
    public ImovelResponseDTO atualizar(Long id, ImovelRequestDTO dto) {
        Imovel imovel = buscarImovelOuFalhar(id);
        boolean trocouCliente = !imovel.getCliente().getId().equals(dto.getClienteId());
        if (trocouCliente && (processoRepository.existsByImovelId(id) || documentoRepository.existsByImovelId(id))) {
            // Os processos/documentos do imovel ficariam ligados a um cliente que nao e mais o dono
            throw new BusinessException("Nao e possivel trocar o cliente deste imovel porque ele possui "
                    + "processos ou documentos vinculados ao cliente atual.");
        }
        copiarDadosParaEntidade(dto, imovel);
        return new ImovelResponseDTO(imovelRepository.save(imovel));
    }

    @Transactional
    public void excluir(Long id) {
        Imovel imovel = buscarImovelOuFalhar(id);
        if (processoRepository.existsByImovelId(id) || documentoRepository.existsByImovelId(id)) {
            throw new BusinessException("Nao e possivel excluir o imovel porque ele possui processos ou "
                    + "documentos vinculados. Exclua esses registros ou retire o imovel deles antes.");
        }
        imovelRepository.delete(imovel);
    }

    private Imovel buscarImovelOuFalhar(Long id) {
        return imovelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Imovel com id " + id + " nao encontrado"));
    }

    private Cliente buscarClienteOuFalhar(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente com id " + clienteId + " nao encontrado"));
    }

    private void copiarDadosParaEntidade(ImovelRequestDTO dto, Imovel imovel) {
        imovel.setCliente(buscarClienteOuFalhar(dto.getClienteId()));
        imovel.setDenominacao(dto.getDenominacao().trim());
        imovel.setMunicipio(dto.getMunicipio().trim());
        imovel.setEstado(dto.getEstado().trim().toUpperCase());
        imovel.setAreaHectares(dto.getAreaHectares());
        imovel.setMatricula(textoOuNulo(dto.getMatricula()));
        imovel.setCartorio(textoOuNulo(dto.getCartorio()));
        imovel.setCodigoCar(textoOuNulo(dto.getCodigoCar()));
        imovel.setCodigoCcir(textoOuNulo(dto.getCodigoCcir()));
        imovel.setNirf(textoOuNulo(dto.getNirf()));
        imovel.setSituacaoGeorreferenciamento(dto.getSituacaoGeorreferenciamento());
        imovel.setObservacoes(textoOuNulo(dto.getObservacoes()));
    }

    // Campos opcionais em branco viram null no banco
    private String textoOuNulo(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
