package br.com.ibt.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;

@Entity
public class Membro extends PanacheEntity {
    public String nome;
    public String email;
    public String telefone;
    public LocalDateTime dataCadastro = LocalDateTime.now();

    public static Membro buscarPorCampos(String nome, String email, String telefone) {
        return find("nome = ?1 and email = ?2 and telefone = ?3", nome, email, telefone).firstResult();
    }
}