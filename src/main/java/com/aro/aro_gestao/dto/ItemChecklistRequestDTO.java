package com.aro.aro_gestao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ItemChecklistRequestDTO {

    @NotBlank(message = "A descricao do item e obrigatoria")
    @Size(max = 200, message = "A descricao do item deve ter no maximo 200 caracteres")
    private String descricao;

    public ItemChecklistRequestDTO() {
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}