package com.aro.aro_gestao.dto;

import com.aro.aro_gestao.entity.StatusProcesso;
import com.aro.aro_gestao.entity.TipoProcesso;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class ProcessoRequestDTO {

    @NotNull(message = "O cliente e obrigatorio")
    @Positive(message = "O clienteId deve ser um numero positivo")
    private Long clienteId;

    // Opcional
    @Positive(message = "O imovelId deve ser um numero positivo")
    private Long imovelId;

    @NotNull(message = "O tipo do processo e obrigatorio")
    private TipoProcesso tipo;

    @NotNull(message = "O status e obrigatorio")
    private StatusProcesso status;

    @Size(max = 60, message = "O numero do protocolo deve ter no maximo 60 caracteres")
    private String numeroProtocolo;

    @Size(max = 100, message = "O orgao deve ter no maximo 100 caracteres")
    private String orgao;

    @NotNull(message = "A data de abertura e obrigatoria")
    private LocalDate dataAbertura;

    private LocalDate dataProtocolo;

    private LocalDate prazoPrevisto;

    private LocalDate dataConclusao;

    @Size(max = 2000, message = "A descricao deve ter no maximo 2000 caracteres")
    private String descricao;

    public ProcessoRequestDTO() {
    }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public Long getImovelId() { return imovelId; }
    public void setImovelId(Long imovelId) { this.imovelId = imovelId; }

    public TipoProcesso getTipo() { return tipo; }
    public void setTipo(TipoProcesso tipo) { this.tipo = tipo; }

    public StatusProcesso getStatus() { return status; }
    public void setStatus(StatusProcesso status) { this.status = status; }

    public String getNumeroProtocolo() { return numeroProtocolo; }
    public void setNumeroProtocolo(String numeroProtocolo) { this.numeroProtocolo = numeroProtocolo; }

    public String getOrgao() { return orgao; }
    public void setOrgao(String orgao) { this.orgao = orgao; }

    public LocalDate getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(LocalDate dataAbertura) { this.dataAbertura = dataAbertura; }

    public LocalDate getDataProtocolo() { return dataProtocolo; }
    public void setDataProtocolo(LocalDate dataProtocolo) { this.dataProtocolo = dataProtocolo; }

    public LocalDate getPrazoPrevisto() { return prazoPrevisto; }
    public void setPrazoPrevisto(LocalDate prazoPrevisto) { this.prazoPrevisto = prazoPrevisto; }

    public LocalDate getDataConclusao() { return dataConclusao; }
    public void setDataConclusao(LocalDate dataConclusao) { this.dataConclusao = dataConclusao; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
