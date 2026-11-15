package com.aro.aro_gestao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

// Configuracoes do sistema. Existe uma unica linha, sempre com id = 1.
@Entity
@Table(name = "configuracoes")
public class Configuracao {

    public static final long ID_UNICO = 1L;
    public static final int DIAS_AVISO_DOCUMENTOS_PADRAO = 30;
    public static final int DIAS_ALERTA_PRAZO_PADRAO = 3;
    public static final String NOME_EMPRESA_PADRAO = "ARO Assessoria";

    @Id
    private Long id = ID_UNICO;

    /* ---------- Dados da empresa (aparecem nos relatorios impressos) ---------- */

    @Column(name = "nome_empresa", nullable = false, length = 150)
    private String nomeEmpresa = NOME_EMPRESA_PADRAO;

    @Column(name = "cnpj", length = 20)
    private String cnpj;

    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "endereco", length = 200)
    private String endereco;

    @Column(name = "cidade", length = 100)
    private String cidade;

    @Column(name = "estado", length = 2)
    private String estado;

    /* ---------- Alertas ---------- */

    // Documento passa a "vencer em breve" quando faltam ate estes dias para a validade
    @Column(name = "dias_aviso_documentos", nullable = false)
    private int diasAvisoDocumentos = DIAS_AVISO_DOCUMENTOS_PADRAO;

    // Prazo de tarefa fica destacado quando faltam ate estes dias
    @Column(name = "dias_alerta_prazo", nullable = false)
    private int diasAlertaPrazo = DIAS_ALERTA_PRAZO_PADRAO;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @Column(name = "atualizado_por", length = 50)
    private String atualizadoPor;

    public Configuracao() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomeEmpresa() { return nomeEmpresa; }
    public void setNomeEmpresa(String nomeEmpresa) { this.nomeEmpresa = nomeEmpresa; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getDiasAvisoDocumentos() { return diasAvisoDocumentos; }
    public void setDiasAvisoDocumentos(int diasAvisoDocumentos) { this.diasAvisoDocumentos = diasAvisoDocumentos; }

    public int getDiasAlertaPrazo() { return diasAlertaPrazo; }
    public void setDiasAlertaPrazo(int diasAlertaPrazo) { this.diasAlertaPrazo = diasAlertaPrazo; }

    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }

    public String getAtualizadoPor() { return atualizadoPor; }
    public void setAtualizadoPor(String atualizadoPor) { this.atualizadoPor = atualizadoPor; }
}
