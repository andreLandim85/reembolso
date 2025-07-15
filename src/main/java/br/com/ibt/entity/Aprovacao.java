package br.com.ibt.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class Aprovacao extends PanacheEntityBase {
    @Id
    public Long id;
    public Long solicitacaoId;
    public String aprovadorId;
    public boolean aprovado;
    public String justificativa;
    public LocalDateTime dataAcao = LocalDateTime.now();
}
