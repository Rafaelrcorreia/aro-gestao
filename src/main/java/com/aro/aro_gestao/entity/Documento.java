package com.aro.aro_gestao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "documentos")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // Opcionais: quando informados, precisam ser do mesmo cliente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imovel_id")
    private Imovel imovel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processo_id")
    private Processo processo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoDocumento tipo;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    // Numero do documento (ex.: numero da certidao ou da matricula)
    @Column(name = "numero", length = 60)
    private String numero;

    @Column(name = "data_emissao")
    private LocalDate dataEmissao;

    // Certidoes e licencas vencem; o sistema avisa quando a data se aproxima
    @Column(name = "data_validade")
    private LocalDate dataValidade;

    @Column(name = "observacoes", length = 1000)
    private String observacoes;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDate dataCadastro;

    // Arquivo (opcional): o nome armazenado e um UUID na pasta de documentos
    @Column(name = "arquivo_nome_original", length = 255)
    private String arquivoNomeOriginal;

    @Column(name = "arquivo_nome_armazenado", length = 60)
    private String arquivoNomeArmazenado;

    @Column(name = "arquivo_tamanho")
    private Long arquivoTamanho;

    public Documento() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Imovel getImovel() { return imovel; }
    public void setImovel(Imovel imovel) { this.imovel = imovel; }

    public Processo getProcesso() { return processo; }
    public void setProcesso(Processo processo) { this.processo = processo; }

    public TipoDocumento getTipo() { return tipo; }
    public void setTipo(TipoDocumento tipo) { this.tipo = tipo; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public LocalDate getDataEmissao() { return dataEmissao; }
    public void setDataEmissao(LocalDate dataEmissao) { this.dataEmissao = dataEmissao; }

    public LocalDate getDataValidade() { return dataValidade; }
    public void setDataValidade(LocalDate dataValidade) { this.dataValidade = dataValidade; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public LocalDate getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDate dataCadastro) { this.dataCadastro = dataCadastro; }

    public String getArquivoNomeOriginal() { return arquivoNomeOriginal; }
    public void setArquivoNomeOriginal(String arquivoNomeOriginal) { this.arquivoNomeOriginal = arquivoNomeOriginal; }

    public String getArquivoNomeArmazenado() { return arquivoNomeArmazenado; }
    public void setArquivoNomeArmazenado(String arquivoNomeArmazenado) { this.arquivoNomeArmazenado = arquivoNomeArmazenado; }

    public Long getArquivoTamanho() { return arquivoTamanho; }
    public void setArquivoTamanho(Long arquivoTamanho) { this.arquivoTamanho = arquivoTamanho; }
}
