package br.com.ibt.service;

import br.com.ibt.entity.Membro;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class MembroService {

    public List<Membro> listarTodos() {
        return Membro.listAll();
    }

    public Membro buscarPorId(Long id) {
        return Membro.findById(id);
    }

    @Transactional
    public Membro criar(Membro membro) {
        membro.persist();
        return membro;
    }

    @Transactional
    public Membro atualizar(Long id, Membro dados) {
        Membro existente = Membro.findById(id);
        if (existente != null) {
            existente.nome = dados.nome;
            existente.email = dados.email;
            existente.telefone = dados.telefone;
        }
        return existente;
    }

    @Transactional
    public boolean deletar(Long id) {
        return Membro.deleteById(id);
    }
}