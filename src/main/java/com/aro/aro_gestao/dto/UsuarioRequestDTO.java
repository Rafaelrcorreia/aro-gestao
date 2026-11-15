package com.aro.aro_gestao.dto;

import com.aro.aro_gestao.entity.PapelUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Usado pelo administrador para criar e editar usuarios.
// A senha so e lida na criacao; para trocar depois, use a redefinicao de senha.
public class UsuarioRequestDTO {

    @NotBlank(message = "O nome e obrigatorio")
    @Size(max = 100, message = "O nome deve ter no maximo 100 caracteres")
    private String nome;

    @NotBlank(message = "O login e obrigatorio")
    @Pattern(regexp = "[A-Za-z0-9._@-]{3,50}",
            message = "O login deve ter de 3 a 50 caracteres: letras, numeros, ponto, hifen, sublinhado ou @")
    private String login;

    @NotNull(message = "O papel e obrigatorio")
    private PapelUsuario papel;

    private Boolean ativo;

    @Size(min = 8, max = 100, message = "A senha deve ter entre 8 e 100 caracteres")
    private String senha;

    public UsuarioRequestDTO() {
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public PapelUsuario getPapel() { return papel; }
    public void setPapel(PapelUsuario papel) { this.papel = papel; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}
