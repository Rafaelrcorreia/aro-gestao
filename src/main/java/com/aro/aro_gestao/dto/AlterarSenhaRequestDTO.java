package com.aro.aro_gestao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Troca de senha. senhaAtual e exigida quando o proprio usuario troca a sua;
// o administrador redefinindo a senha de outra pessoa envia so a novaSenha.
public class AlterarSenhaRequestDTO {

    private String senhaAtual;

    @NotBlank(message = "A nova senha e obrigatoria")
    @Size(min = 8, max = 100, message = "A nova senha deve ter entre 8 e 100 caracteres")
    private String novaSenha;

    public AlterarSenhaRequestDTO() {
    }

    public String getSenhaAtual() { return senhaAtual; }
    public void setSenhaAtual(String senhaAtual) { this.senhaAtual = senhaAtual; }

    public String getNovaSenha() { return novaSenha; }
    public void setNovaSenha(String novaSenha) { this.novaSenha = novaSenha; }
}
