package com.umc.pediupatrao.entity;

import com.umc.pediupatrao.entity.enums.RoleEnum;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "usuarios")
public class Usuario {

    @Id
    private String id;
    private String username;
    private String password;
    private String email;
    private RoleEnum role;
    private int tentativasCodigo = 0;
    private LocalDateTime bloqueioCodigoAte;

    // Campos para controle de recuperação de senha
    private String codigoRecuperacao;
    private LocalDateTime expiracaoCodigo;

    // Getters e Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCodigoRecuperacao() { return codigoRecuperacao; }
    public void setCodigoRecuperacao(String codigoRecuperacao) { this.codigoRecuperacao = codigoRecuperacao; }

    public LocalDateTime getExpiracaoCodigo() { return expiracaoCodigo; }
    public void setExpiracaoCodigo(LocalDateTime expiracaoCodigo) { this.expiracaoCodigo = expiracaoCodigo; }

    public int getTentativasCodigo() { return tentativasCodigo; }
    public void setTentativasCodigo(int tentativasCodigo) { this.tentativasCodigo = tentativasCodigo; }

    public LocalDateTime getBloqueioCodigoAte() { return bloqueioCodigoAte; }
    public void setBloqueioCodigoAte(LocalDateTime bloqueioCodigoAte) { this.bloqueioCodigoAte = bloqueioCodigoAte; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public RoleEnum getRole() { return role; }
    public void setRole(RoleEnum role) { this.role = role; }
}