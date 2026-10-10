package com.umc.pediupatrao.entity;

import com.umc.pediupatrao.entity.enums.RoleEnum;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "auditoria")
public class Auditoria {

    @Id
    private String id;
    private String usuario;
    private RoleEnum role;
    private LocalDateTime dataHora;
    private String operacao;
    private String entidadeAfetada;
    private String idEntidade;
    private String campoModificado;
    private String valorAnterior;
    private String valorNovo;
    private String justificativa;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public RoleEnum getRole() { return role; }
    public void setRole(RoleEnum role) { this.role = role; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public String getOperacao() { return operacao; }
    public void setOperacao(String operacao) { this.operacao = operacao; }

    public String getEntidadeAfetada() { return entidadeAfetada; }
    public void setEntidadeAfetada(String entidadeAfetada) { this.entidadeAfetada = entidadeAfetada; }

    public String getIdEntidade() { return idEntidade; }
    public void setIdEntidade(String idEntidade) { this.idEntidade = idEntidade; }

    public String getCampoModificado() { return campoModificado; }
    public void setCampoModificado(String campoModificado) { this.campoModificado = campoModificado; }

    public String getValorAnterior() { return valorAnterior; }
    public void setValorAnterior(String valorAnterior) { this.valorAnterior = valorAnterior; }

    public String getValorNovo() { return valorNovo; }
    public void setValorNovo(String valorNovo) { this.valorNovo = valorNovo; }

    public String getJustificativa() { return justificativa; }
    public void setJustificativa(String justificativa) { this.justificativa = justificativa; }
}