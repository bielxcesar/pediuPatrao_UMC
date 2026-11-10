package com.umc.pediupatrao.entity;

import java.math.BigDecimal;

public class ItemPedido {

    private String produtoId;
    private Integer quantidade;
    private BigDecimal precoUnitario;
    //Usando o BigDecimal para lidar com valores monetários porque ele evita falhas matemáticas
    // de precisão e arredondamento que ocorrem com tipos como double ou float.

    public String getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(String produtoId) {
        this.produtoId = produtoId;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPrecoUnitario(){
        return precoUnitario;
    }
    public void setPrecoUnitario (BigDecimal PrecoUnitario){
        this.precoUnitario = precoUnitario;
    }

}
