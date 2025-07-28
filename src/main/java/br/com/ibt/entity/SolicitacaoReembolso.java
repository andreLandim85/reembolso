package br.com.ibt.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Lob;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
public class SolicitacaoReembolso extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "membro_id")
    public Membro membro;
    public String descricao;
    public Double valor;
    public Status status = Status.PENDENTE;
    public String justificativa;
    public String chavePix;
    public LocalDateTime dataCriacao = LocalDateTime.now();
    @ManyToOne
    @JoinColumn(name = "aprovador_id")
    public Membro aprovador;

    @ManyToOne
    @JoinColumn(name = "tesoureiro_aprovador_id")
    public Membro tesoureiroAprovador;

    public LocalDateTime dataAprovacao;
    public String tipoChave;
    @Lob
    public byte[] notaFiscal; // armazena o arquivo binário

    public String notaFiscalNome; // opcional: nome do arquivo
    public String notaFiscalTipo; // opcional: tipo MIME

    public Membro getAprovador() {
        return aprovador;
    }

    public void setAprovador(Membro aprovador) {
        this.aprovador = aprovador;
    }

    public Membro getTesoureiroAprovador() {
        return tesoureiroAprovador;
    }

    public void setTesoureiroAprovador(Membro tesoureiroAprovador) {
        this.tesoureiroAprovador = tesoureiroAprovador;
    }

    public String getDataCriacaoFormatada() {
        if (dataCriacao == null) return "";
        return dataCriacao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }


    public String getDataAprovacaoFormatada() {
        if (dataAprovacao == null) return "";
        return dataAprovacao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public enum Status {
        PENDENTE,
        APROVADA,
        REJEITADA,
        AGUARDANDO_PAGAMENTO,
        CONCLUIDA,
        PAGAMENTO_REJEITADO
    }
}