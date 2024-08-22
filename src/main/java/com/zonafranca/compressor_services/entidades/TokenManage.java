package com.zonafranca.compressor_services.entidades;

import java.util.Date;
 
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
 
@Entity
@Table(name = "TZFW_GESTION_TOKENS")
public class TokenManage {
    @Id
    @Column(name = "ID")
    private Integer id;
 
    @Column(name = "CDPROCESO")
    private String process;
 
    @Column(name = "CBTOKEN")
    private String token;
 
    @Column(name = "CDUSERNAME")
    private String username;
 
    @Column(name = "DTEXPIRACION")
    private Date expiration;
 
    public Integer getId() {
        return this.id;
    }
 
    public void setId(Integer id) {
        this.id = id;
    }
 
    public String getProcess() {
        return this.process;
    }
 
    public void setProcess(String process) {
        this.process = process;
    }
 
    public String getToken() {
        return this.token;
    }
 
    public void setToken(String token) {
        this.token = token;
    }
 
    public String getUsername() {
        return this.username;
    }
 
    public void setUsername(String username) {
        this.username = username;
    }
 
    public Date getExpiration() {
        return this.expiration;
    }
 
    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }
 
}