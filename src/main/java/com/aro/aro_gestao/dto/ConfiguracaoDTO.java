package com.aro.aro_gestao.dto;

import com.aro.aro_gestao.entity.Configuracao;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

// Usado tanto para ler quanto para salvar as configuracoes.
// atualizadoEm/atualizadoPor sao so de leitura (ignorados ao salvar).
public class ConfiguracaoDTO {

    @NotBlank(message = "O nome da empresa e obrigatorio")
    @Size(max = 150, message = "O nome da empresa deve ter no maximo 150 caracteres")
    private String nomeEmpresa;

    @Size(max = 20, message = "O CNPJ deve ter no maximo 20 caracteres")
    private String cnpj;

    @Size(max = 20, message = "O telefone deve ter no maximo 20 caracteres")
    private String telefone;

    @Email(message = "O e-mail informado nao e valido")
    @Size(max = 150, message = "O e-mail deve ter no maximo 150 caracteres")
    private String email;

    @Size(max = 200, message = "O endereco deve ter no maximo 200 caracteres")
    private String endereco;

    @Size(max = 100, message = "A cidade deve ter no maximo 100 caracteres")
    private String cidade;

    @Pattern(regexp = "^$|[A-Za-z]{2}", message = "O estado deve ser a sigla com 2 letras")
    private String estado;

    @NotNull(message = "Informe os dias de aviso de vencimento de documentos")
    @Min(value = 1, message = "O aviso de documentos deve ser de 1 a 365 dias")
    @Max(value = 365, message = "O aviso de documentos deve ser de 1 a 365 dias")
    private Integer diasAvisoDocumentos;

    @NotNull(message = "Informe os dias de alerta de prazo")
    @Min(value = 0, message = "O alerta de prazo deve ser de 0 a 60 dias")
    @Max(value = 60, message = "O alerta de prazo deve ser de 0 a 60 dias")
    private Integer diasAlertaPrazo;

    private LocalDateTime atualizadoEm;
    private String atualizadoPor;

    public ConfiguracaoDTO() {
    }

    public ConfiguracaoDTO(Configuracao configuracao) {
        this.nomeEmpresa = configuracao.getNomeEmpresa();
        this.cnpj = configuracao.getCnpj();
        this.telefone = configuracao.getTelefone();
        this.email = configuracao.getEmail();
        this.endereco = configuracao.getEndereco();
        this.cidade = configuracao.getCidade();
        this.estado = configuracao.getEstado();
        this.diasAvisoDocumentos = configuracao.getDiasAvisoDocumentos();
        this.diasAlertaPrazo = configuracao.getDiasAlertaPrazo();
        this.atualizadoEm = configuracao.getAtualizadoEm();
        this.atualizadoPor = configuracao.getAtualizadoPor();
    }

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

    public Integer getDiasAvisoDocumentos() { return diasAvisoDocumentos; }
    public void setDiasAvisoDocumentos(Integer diasAvisoDocumentos) { this.diasAvisoDocumentos = diasAvisoDocumentos; }

    public Integer getDiasAlertaPrazo() { return diasAlertaPrazo; }
    public void setDiasAlertaPrazo(Integer diasAlertaPrazo) { this.diasAlertaPrazo = diasAlertaPrazo; }

    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm) { this.atualizadoEm = atualizadoEm; }

    public String getAtualizadoPor() { return atualizadoPor; }
    public void setAtualizadoPor(String atualizadoPor) { this.atualizadoPor = atualizadoPor; }
}
