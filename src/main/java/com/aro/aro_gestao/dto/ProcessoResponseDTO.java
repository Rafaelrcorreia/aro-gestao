package com.aro.aro_gestao.dto;

import com.aro.aro_gestao.entity.Imovel;
import com.aro.aro_gestao.entity.Processo;
import com.aro.aro_gestao.entity.StatusProcesso;
import com.aro.aro_gestao.entity.TipoProcesso;

import java.time.LocalDate;

public class ProcessoResponseDTO {

    private final Long id;
    private final Long clienteId;
    private final String nomeCliente;
    private final Long imovelId;
    private final String denominacaoImovel;
    private final TipoProcesso tipo;
    private final StatusProcesso status;
    private final String numeroProtocolo;
    private final String orgao;
    private final LocalDate dataAbertura;
    private final LocalDate dataProtocolo;
    private final LocalDate prazoPrevisto;
    private final LocalDate dataConclusao;
    private final String descricao;
    private final boolean atrasado;

    public ProcessoResponseDTO(Processo processo) {
        this.id = processo.getId();
        this.clienteId = processo.getCliente().getId();
        this.nomeCliente = processo.getCliente().getNomeCompleto();
        Imovel imovel = processo.getImovel();
        this.imovelId = imovel == null ? null : imovel.getId();
        this.denominacaoImovel = imovel == null ? null : imovel.getDenominacao();
        this.tipo = processo.getTipo();
        this.status = processo.getStatus();
        this.numeroProtocolo = processo.getNumeroProtocolo();
        this.orgao = processo.getOrgao();
        this.dataAbertura = processo.getDataAbertura();
        this.dataProtocolo = processo.getDataProtocolo();
        this.prazoPrevisto = processo.getPrazoPrevisto();
        this.dataConclusao = processo.getDataConclusao();
        this.descricao = processo.getDescricao();
        // Atrasado = ainda em andamento e com prazo previsto antes de hoje
        this.atrasado = processo.getStatus() != null
                && !processo.getStatus().isFinalizado()
                && processo.getPrazoPrevisto() != null
                && processo.getPrazoPrevisto().isBefore(LocalDate.now());
    }

    public Long getId() { return id; }
    public Long getClienteId() { return clienteId; }
    public String getNomeCliente() { return nomeCliente; }
    public Long getImovelId() { return imovelId; }
    public String getDenominacaoImovel() { return denominacaoImovel; }
    public TipoProcesso getTipo() { return tipo; }
    public StatusProcesso getStatus() { return status; }
    public String getNumeroProtocolo() { return numeroProtocolo; }
    public String getOrgao() { return orgao; }
    public LocalDate getDataAbertura() { return dataAbertura; }
    public LocalDate getDataProtocolo() { return dataProtocolo; }
    public LocalDate getPrazoPrevisto() { return prazoPrevisto; }
    public LocalDate getDataConclusao() { return dataConclusao; }
    public String getDescricao() { return descricao; }
    public boolean isAtrasado() { return atrasado; }
}
