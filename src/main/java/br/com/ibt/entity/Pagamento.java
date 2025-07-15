package br.com.ibt.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;

@Entity
public class Pagamento extends PanacheEntity {
    public Long solicitacaoId;
    public String tesoureiroId;
    public boolean concluido;
    public String justificativa;
    public LocalDateTime dataPagamento = LocalDateTime.now();
}