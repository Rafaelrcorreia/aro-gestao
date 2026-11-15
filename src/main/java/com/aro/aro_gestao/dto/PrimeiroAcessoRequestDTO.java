package com.aro.aro_gestao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Cria o primeiro administrador. So funciona enquanto nao existe nenhum usuario.
public class PrimeiroAcessoRequestDTO {

    @NotBlank(message = "O nome e obrigatorio")
    @Size(max = 100, message = "O nome deve ter no maximo 100 caracteres")
    private String nome;

    @NotBlank(message = "O login e obrigatorio")
    @Pattern(regexp = "[A-Za-z0-9._@-]{3,50}",
            message = "O login deve ter de 3 a 50 caracteres: letras, numeros, ponto, hifen, sublinhado ou @")
    private String login;

    @NotBlank(message = "A senha e obrigatoria")
    @Size(min = 8, max = 100, message = "A senha deve ter entre 8 e 100 caracteres")
    private String senha;

    public PrimeiroAcessoRequestDTO() {
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}
