package com.aro.aro_gestao.dto;

import com.aro.aro_gestao.entity.TipoDocumento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

// Vai como a parte "dados" (JSON) do envio multipart; o arquivo vai na parte "arquivo"
public class DocumentoRequestDTO {

    @NotNull(message = "O cliente e obrigatorio")
    @Positive(message = "O clienteId deve ser um numero positivo")
    private Long clienteId;

    @Positive(message = "O imovelId deve ser um numero positivo")
    private Long imovelId;

    @Positive(message = "O processoId deve ser um numero positivo")
    private Long processoId;

    @NotNull(message = "O tipo do documento e obrigatorio")
    private TipoDocumento tipo;

    @NotBlank(message = "O titulo e obrigatorio")
    @Size(max = 150, message = "O titulo deve ter no maximo 150 caracteres")
    private String titulo;

    @Size(max = 60, message = "O numero deve ter no maximo 60 caracteres")
    private String numero;

    private LocalDate dataEmissao;

    private LocalDate dataValidade;

    @Size(max = 1000, message = "As observacoes devem ter no maximo 1000 caracteres")
    private String observacoes;

    public DocumentoRequestDTO() {
    }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public Long getImovelId() { return imovelId; }
    public void setImovelId(Long imovelId) { this.imovelId = imovelId; }

    public Long getProcessoId() { return processoId; }
    public void setProcessoId(Long processoId) { this.processoId = processoId; }

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
}
