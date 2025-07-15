package br.com.ibt.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;

@Entity
public class SolicitacaoReembolso extends PanacheEntity {
    public Long membroId;
    public String descricao;
    public Double valor;
    public Status status = Status.PENDENTE;
    public LocalDateTime dataCriacao = LocalDateTime.now();

    public enum Status {
        PENDENTE,
        APROVADA,
        REJEITADA,
        AGUARDANDO_NOTA,
        AGUARDANDO_PAGAMENTO,
        CONCLUIDA
    }
}