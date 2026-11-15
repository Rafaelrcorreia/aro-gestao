package com.aro.aro_gestao.service;

import com.aro.aro_gestao.dto.DocumentoRequestDTO;
import com.aro.aro_gestao.dto.DocumentoResponseDTO;
import com.aro.aro_gestao.entity.Cliente;
import com.aro.aro_gestao.entity.Documento;
import com.aro.aro_gestao.entity.Imovel;
import com.aro.aro_gestao.entity.Processo;
import com.aro.aro_gestao.entity.TipoDocumento;
import com.aro.aro_gestao.exception.BusinessException;
import com.aro.aro_gestao.exception.ResourceNotFoundException;
import com.aro.aro_gestao.repository.ClienteRepository;
import com.aro.aro_gestao.repository.DocumentoRepository;
import com.aro.aro_gestao.repository.ImovelRepository;
import com.aro.aro_gestao.repository.ProcessoRepository;
import com.aro.aro_gestao.service.ArmazenamentoDocumentosService.ArquivoSalvo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

@Service
public class DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final ClienteRepository clienteRepository;
    private final ImovelRepository imovelRepository;
    private final ProcessoRepository processoRepository;
    private final ArmazenamentoDocumentosService armazenamento;
    private final ConfiguracaoService configuracaoService;

    public DocumentoService(DocumentoRepository documentoRepository,
                            ClienteRepository clienteRepository,
                            ImovelRepository imovelRepository,
                            ProcessoRepository processoRepository,
                            ArmazenamentoDocumentosService armazenamento,
                            ConfiguracaoService configuracaoService) {
        this.documentoRepository = documentoRepository;
        this.clienteRepository = clienteRepository;
        this.imovelRepository = imovelRepository;
        this.processoRepository = processoRepository;
        this.armazenamento = armazenamento;
        this.configuracaoService = configuracaoService;
    }

    private DocumentoResponseDTO paraResposta(Documento documento) {
        return new DocumentoResponseDTO(documento, configuracaoService.diasAvisoDocumentos());
    }

    /** Dados do arquivo para download/visualizacao. tipoExibivel nulo = forcar download. */
    public record ArquivoDocumento(Path caminho, String nomeOriginal, String tipoExibivel) {
    }

    // Criar/atualizar nao usam @Transactional de proposito: o arquivo e gravado no disco
    // antes de salvar no banco, e se o banco falhar o arquivo novo e apagado no catch.
    public DocumentoResponseDTO criar(DocumentoRequestDTO dto, MultipartFile arquivo) {
        Documento documento = new Documento();
        copiarDadosParaEntidade(dto, documento);
        documento.setDataCadastro(LocalDate.now());

        ArquivoSalvo novo = salvarArquivoSeEnviado(arquivo, documento);
        try {
            return paraResposta(documentoRepository.save(documento));
        } catch (RuntimeException ex) {
            excluirArquivo(novo);
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public List<DocumentoResponseDTO> listar(Long clienteId, TipoDocumento tipo, String busca) {
        if (clienteId != null) {
            buscarClienteOuFalhar(clienteId);
        }
        String termo = (busca == null || busca.isBlank()) ? null : busca.trim();

        int diasAviso = configuracaoService.diasAvisoDocumentos();
        return documentoRepository.buscar(clienteId, tipo, termo)
                .stream()
                .map(documento -> new DocumentoResponseDTO(documento, diasAviso))
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentoResponseDTO buscarPorId(Long id) {
        return paraResposta(buscarDocumentoOuFalhar(id));
    }

    public DocumentoResponseDTO atualizar(Long id, DocumentoRequestDTO dto, MultipartFile arquivo) {
        Documento documento = buscarDocumentoOuFalhar(id);
        copiarDadosParaEntidade(dto, documento);

        String arquivoAntigo = documento.getArquivoNomeArmazenado();
        ArquivoSalvo novo = salvarArquivoSeEnviado(arquivo, documento);
        DocumentoResponseDTO resposta;
        try {
            resposta = paraResposta(documentoRepository.save(documento));
        } catch (RuntimeException ex) {
            excluirArquivo(novo);
            throw ex;
        }
        // So apaga o arquivo antigo depois que o banco ja aponta para o novo
        if (novo != null) {
            armazenamento.excluir(arquivoAntigo);
        }
        return resposta;
    }

    public void excluir(Long id) {
        Documento documento = buscarDocumentoOuFalhar(id);
        String arquivo = documento.getArquivoNomeArmazenado();
        documentoRepository.delete(documento);
        armazenamento.excluir(arquivo);
    }

    public ArquivoDocumento buscarArquivo(Long id) {
        Documento documento = documentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento com id " + id + " nao encontrado"));
        if (documento.getArquivoNomeArmazenado() == null) {
            throw new ResourceNotFoundException("Este documento nao possui arquivo anexado.");
        }
        String nomeArmazenado = documento.getArquivoNomeArmazenado();
        return new ArquivoDocumento(
                armazenamento.caminhoDe(nomeArmazenado),
                documento.getArquivoNomeOriginal(),
                armazenamento.tipoExibivel(nomeArmazenado));
    }

    private ArquivoSalvo salvarArquivoSeEnviado(MultipartFile arquivo, Documento documento) {
        if (arquivo == null || arquivo.isEmpty()) {
            return null;
        }
        ArquivoSalvo salvo = armazenamento.salvar(arquivo);
        documento.setArquivoNomeArmazenado(salvo.nomeArmazenado());
        documento.setArquivoNomeOriginal(salvo.nomeOriginal());
        documento.setArquivoTamanho(salvo.tamanho());
        return salvo;
    }

    private void excluirArquivo(ArquivoSalvo arquivo) {
        if (arquivo != null) {
            armazenamento.excluir(arquivo.nomeArmazenado());
        }
    }

    private Documento buscarDocumentoOuFalhar(Long id) {
        return documentoRepository.findComVinculosById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Documento com id " + id + " nao encontrado"));
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

    private Processo buscarProcessoDoCliente(Long processoId, Cliente cliente) {
        Processo processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Processo com id " + processoId + " nao encontrado"));
        if (!processo.getCliente().getId().equals(cliente.getId())) {
            throw new BusinessException("O processo informado nao pertence ao cliente selecionado.");
        }
        return processo;
    }

    private void copiarDadosParaEntidade(DocumentoRequestDTO dto, Documento documento) {
        if (dto.getDataEmissao() != null && dto.getDataValidade() != null
                && dto.getDataValidade().isBefore(dto.getDataEmissao())) {
            throw new BusinessException("A data de validade nao pode ser anterior a data de emissao.");
        }
        Cliente cliente = buscarClienteOuFalhar(dto.getClienteId());

        documento.setCliente(cliente);
        documento.setImovel(dto.getImovelId() == null ? null : buscarImovelDoCliente(dto.getImovelId(), cliente));
        documento.setProcesso(dto.getProcessoId() == null ? null : buscarProcessoDoCliente(dto.getProcessoId(), cliente));
        documento.setTipo(dto.getTipo());
        documento.setTitulo(dto.getTitulo().trim());
        documento.setNumero(textoOuNulo(dto.getNumero()));
        documento.setDataEmissao(dto.getDataEmissao());
        documento.setDataValidade(dto.getDataValidade());
        documento.setObservacoes(textoOuNulo(dto.getObservacoes()));
    }

    // Campos opcionais em branco viram null no banco
    private String textoOuNulo(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
