package br.com.ibt.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;

@Entity
public class DocumentoAnexo extends PanacheEntity {
    public Long solicitacaoId;
    public Tipo tipo;
    public String url;
    public LocalDateTime dataUpload = LocalDateTime.now();

    public enum Tipo {
        NOTA_FISCAL,
        COMPROVANTE_PAGAMENTO
    }
}