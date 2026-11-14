package com.aro.aro_gestao.dto;

import com.aro.aro_gestao.entity.ItemChecklist;

public class ItemChecklistResponseDTO {

    private Long id;
    private String descricao;
    private boolean concluido;

    public ItemChecklistResponseDTO() {
    }

    public ItemChecklistResponseDTO(ItemChecklist item) {
        this.id = item.getId();
        this.descricao = item.getDescricao();
        this.concluido = item.isConcluido();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public boolean isConcluido() {
        return concluido;
    }

    public void setConcluido(boolean concluido) {
        this.concluido = concluido;
    }
}
