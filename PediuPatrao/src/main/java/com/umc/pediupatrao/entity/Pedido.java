package com.umc.pediupatrao.entity;

import jakarta.persistence.Entity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "pedidos")

public class Pedido {
    @Id
    private String id;
    private String clienteId;
    private String StatusPedidoEnum;

    private List<ItemPedido> itens;
    private BigDecimal valorTotal;
    private BigDecimal desconto;

    private String responsavelRegistro;
    private String responsavelSaida;

    private LocalDateTime dataHoraEntrada;
    private LocalDateTime dataHoraSaida;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getStatusPedidoEnum() {
        return StatusPedidoEnum;
    }

    public void setStatusPedidoEnum(String StatusPedidoEnum) {
        this.StatusPedidoEnum = StatusPedidoEnum;
    }

    public List<ItemPedido> getItens() {return itens;}

    public void setItens(List<ItemPedido> itens) {this.itens = itens;}

    public BigDecimal getValorTotal () {return valorTotal;}

    public void setValorTotal (BigDecimal valorTotal){this.valorTotal = valorTotal;}

    public BigDecimal getDesconto() { return desconto;}

    public void setDesconto (BigDecimal desconto) {this.desconto = desconto;}

    public String getResponsavelRegistro (){return responsavelRegistro;}

    public void setResponsavelRegistro(String responsavelRegistro) {this.responsavelRegistro = responsavelRegistro;}

    public String getResponsavelSaida() {return  responsavelSaida;}

    public void setResponsavelSaida(String responsavelSaida) {this.responsavelSaida = responsavelSaida;}

    public LocalDateTime getDataHoraEntrada() {return dataHoraEntrada;}
    public void setDataHoraEntrada  (LocalDateTime dataHoraEntrada){this.dataHoraEntrada = dataHoraEntrada;}

    public LocalDateTime getDataHoraSaida() {return dataHoraSaida;}

    public void setDataHoraSaida (LocalDateTime dataHoraSaida) {this.dataHoraSaida = dataHoraSaida;}
}
