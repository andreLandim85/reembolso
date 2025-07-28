package br.com.ibt.service;

import br.com.ibt.entity.DocumentoAnexo;
import br.com.ibt.entity.Membro;
import br.com.ibt.entity.Pagamento;
import br.com.ibt.entity.SolicitacaoReembolso;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class SolicitacaoReembolsoService {

    public SolicitacaoReembolso criarSolicitacao(String descricao, Double valor, String nome, String telefone, String email) {
        Membro m = getMembro(nome, telefone, email);

        SolicitacaoReembolso s = new SolicitacaoReembolso();
        s.membro = m;
        s.descricao = descricao;
        s.valor = valor;
        s.status = SolicitacaoReembolso.Status.PENDENTE;
        s.persist();
        return s;
    }

    /**
     * Busca um membro na base. Cria um novo registro caso não encontre
     * @param nome
     * @param telefone
     * @param email
     * @return
     */
    @Transactional
    public static Membro getMembro(String nome, String telefone, String email) {
        Membro m = Membro.buscarPorCampos(nome, email, telefone);
        if (m == null) {
            m = new Membro();
            m.nome = nome;
            m.email = email;
            m.telefone = telefone;
            m.persist();
            Log.debug("Novo membro salvo!");
        } else {
            Log.debug("Membro já existente!");
        }
        return m;
    }

    @Transactional
    public  SolicitacaoReembolso buscarPorId(Long id) {
        return SolicitacaoReembolso.findById(id);
    }

    @Transactional
    public SolicitacaoReembolso atualizarSolicitacao(Long id, String urlNota, String chavePix) {
        SolicitacaoReembolso s = SolicitacaoReembolso.findById(id);
        if (s != null && s.status == SolicitacaoReembolso.Status.APROVADA) {
            DocumentoAnexo anexo = new DocumentoAnexo();
            anexo.solicitacaoId = id;
            anexo.tipo = DocumentoAnexo.Tipo.NOTA_FISCAL;
            anexo.url = urlNota;
            anexo.persist();
            s.status = SolicitacaoReembolso.Status.AGUARDANDO_PAGAMENTO;
            s.chavePix = chavePix;
            s.persist();
        }
        return s;
    }

    @Transactional
    public SolicitacaoReembolso definirStatus(Long id, SolicitacaoReembolso.Status status, String justificativa, Membro m) {
        SolicitacaoReembolso s = SolicitacaoReembolso.findById(id);
        if (s != null) {
            s.status = status;
            s.justificativa = justificativa;
            s.aprovador = m;
            s.dataAprovacao = LocalDateTime.now();
            s.persist();
        }
        return s;
    }

    @Transactional
    public SolicitacaoReembolso processarPagamento(Long id, Membro tesoureiro, SolicitacaoReembolso.Status status ,String justificativa) {
        SolicitacaoReembolso s = SolicitacaoReembolso.findById(id);
        if (s != null && s.status == SolicitacaoReembolso.Status.AGUARDANDO_PAGAMENTO) {
            s.status = status ;
            s.dataAprovacaoTesoureiro = LocalDateTime.now();
            s.setTesoureiroAprovador(tesoureiro);
            s.justificativaTesoureiro = justificativa;
            s.persist();
        }
        return s;
    }

    @Transactional
    public List<SolicitacaoReembolso> listarPendentes() {
        return SolicitacaoReembolso.list("status", SolicitacaoReembolso.Status.PENDENTE);
    }

    @Transactional
    public List<SolicitacaoReembolso> listarUltimasPorMembro(Long membroId) {
        return SolicitacaoReembolso.find("membro.id = ?1 ORDER BY dataCriacao DESC", membroId)
                .page(0, 10)
                .list();
    }


    @Transactional
    public List<SolicitacaoReembolso> listarAguardandoPagamento() {
        return SolicitacaoReembolso.list("status", SolicitacaoReembolso.Status.AGUARDANDO_PAGAMENTO);
    }

    @Transactional
    public void anexarNotaFiscal(Long id, byte[] nota, String nome, String tipo, String chavePix, String tipoChavePix) {
        SolicitacaoReembolso s = SolicitacaoReembolso.findById(id);
        if (s != null) {
            s.notaFiscal = nota;
            s.notaFiscalNome = nome;
            s.notaFiscalTipo = tipo;
            s.chavePix = chavePix;
            s.tipoChave = tipoChavePix;
            s.status = SolicitacaoReembolso.Status.AGUARDANDO_PAGAMENTO;
            s.persist();
        }
    }
}