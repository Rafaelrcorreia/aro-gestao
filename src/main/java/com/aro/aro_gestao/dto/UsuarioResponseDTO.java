package com.aro.aro_gestao.dto;

import com.aro.aro_gestao.entity.PapelUsuario;
import com.aro.aro_gestao.entity.Usuario;

import java.time.LocalDateTime;

// Nunca inclui a senha nem o hash
public class UsuarioResponseDTO {

    private final Long id;
    private final String nome;
    private final String login;
    private final PapelUsuario papel;
    private final boolean ativo;
    private final LocalDateTime dataCadastro;

    public UsuarioResponseDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.login = usuario.getLogin();
        this.papel = usuario.getPapel();
        this.ativo = usuario.isAtivo();
        this.dataCadastro = usuario.getDataCadastro();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getLogin() { return login; }
    public PapelUsuario getPapel() { return papel; }
    public boolean isAtivo() { return ativo; }
    public LocalDateTime getDataCadastro() { return dataCadastro; }
}
