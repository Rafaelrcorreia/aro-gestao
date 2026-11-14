package com.aro.aro_gestao.dto;

import com.aro.aro_gestao.entity.Documento;
import com.aro.aro_gestao.entity.Imovel;
import com.aro.aro_gestao.entity.Processo;
import com.aro.aro_gestao.entity.TipoDocumento;

import java.time.LocalDate;

public class DocumentoResponseDTO {

    // Quantos dias antes do vencimento o documento passa a ser "vencendo"
    public static final int DIAS_AVISO_VENCIMENTO = 30;

    public enum SituacaoValidade {
        SEM_VALIDADE,
        VALIDO,
        VENCENDO,
        VENCIDO
    }

    private final Long id;
    private final Long clienteId;
    private final String nomeCliente;
    private final Long imovelId;
    private final String denominacaoImovel;
    private final Long processoId;
    private final String descricaoProcesso;
    private final TipoDocumento tipo;
    private final String titulo;
    private final String numero;
    private final LocalDate dataEmissao;
    private final LocalDate dataValidade;
    private final SituacaoValidade situacaoValidade;
    private final String observacoes;
    private final LocalDate dataCadastro;
    private final boolean possuiArquivo;
    private final String arquivoNome;
    private final Long arquivoTamanho;

    public DocumentoResponseDTO(Documento documento) {
        this.id = documento.getId();
        this.clienteId = documento.getCliente().getId();
        this.nomeCliente = documento.getCliente().getNomeCompleto();

        Imovel imovel = documento.getImovel();
        this.imovelId = imovel == null ? null : imovel.getId();
        this.denominacaoImovel = imovel == null ? null : imovel.getDenominacao();

        Processo processo = documento.getProcesso();
        this.processoId = processo == null ? null : processo.getId();
        this.descricaoProcesso = processo == null ? null : descreverProcesso(processo);

        this.tipo = documento.getTipo();
        this.titulo = documento.getTitulo();
        this.numero = documento.getNumero();
        this.dataEmissao = documento.getDataEmissao();
        this.dataValidade = documento.getDataValidade();
        this.situacaoValidade = calcularSituacao(documento.getDataValidade(), LocalDate.now());
        this.observacoes = documento.getObservacoes();
        this.dataCadastro = documento.getDataCadastro();
        this.possuiArquivo = documento.getArquivoNomeArmazenado() != null;
        this.arquivoNome = documento.getArquivoNomeOriginal();
        this.arquivoTamanho = documento.getArquivoTamanho();
    }

    public static SituacaoValidade calcularSituacao(LocalDate validade, LocalDate hoje) {
        if (validade == null) {
            return SituacaoValidade.SEM_VALIDADE;
        }
        if (validade.isBefore(hoje)) {
            return SituacaoValidade.VENCIDO;
        }
        if (!validade.isAfter(hoje.plusDays(DIAS_AVISO_VENCIMENTO))) {
            return SituacaoValidade.VENCENDO;
        }
        return SituacaoValidade.VALIDO;
    }

    // Ex.: "Georreferenciamento - SIGEF-123"
    private static String descreverProcesso(Processo processo) {
        String descricao = processo.getTipo().getRotulo();
        if (processo.getNumeroProtocolo() != null) {
            descricao += " - " + processo.getNumeroProtocolo();
        }
        return descricao;
    }

    public Long getId() { return id; }
    public Long getClienteId() { return clienteId; }
    public String getNomeCliente() { return nomeCliente; }
    public Long getImovelId() { return imovelId; }
    public String getDenominacaoImovel() { return denominacaoImovel; }
    public Long getProcessoId() { return processoId; }
    public String getDescricaoProcesso() { return descricaoProcesso; }
    public TipoDocumento getTipo() { return tipo; }
    public String getTitulo() { return titulo; }
    public String getNumero() { return numero; }
    public LocalDate getDataEmissao() { return dataEmissao; }
    public LocalDate getDataValidade() { return dataValidade; }
    public SituacaoValidade getSituacaoValidade() { return situacaoValidade; }
    public String getObservacoes() { return observacoes; }
    public LocalDate getDataCadastro() { return dataCadastro; }
    public boolean isPossuiArquivo() { return possuiArquivo; }
    public String getArquivoNome() { return arquivoNome; }
    public Long getArquivoTamanho() { return arquivoTamanho; }
}
