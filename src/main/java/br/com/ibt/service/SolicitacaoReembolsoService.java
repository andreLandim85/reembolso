package br.com.ibt.service;

import br.com.ibt.entity.Aprovacao;
import br.com.ibt.entity.DocumentoAnexo;
import br.com.ibt.entity.Pagamento;
import br.com.ibt.entity.SolicitacaoReembolso;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class SolicitacaoReembolsoService {

    public SolicitacaoReembolso criarSolicitacao(Long membroId, String descricao, Double valor) {
        SolicitacaoReembolso s = new SolicitacaoReembolso();
        s.membroId = membroId;
        s.descricao = descricao;
        s.valor = valor;
        s.status = SolicitacaoReembolso.Status.PENDENTE;
        s.persist();
        return s;
    }

    public List<SolicitacaoReembolso> listarMinhasSolicitacoes(Long membroId) {
        return SolicitacaoReembolso.list("membroId", membroId);
    }

    @Transactional
    public SolicitacaoReembolso anexarNotaFiscal(Long id, String urlNota, String chavePix) {
        SolicitacaoReembolso s = SolicitacaoReembolso.findById(id);
        if (s != null && s.status == SolicitacaoReembolso.Status.APROVADA) {
            DocumentoAnexo anexo = new DocumentoAnexo();
            anexo.solicitacaoId = id;
            anexo.tipo = DocumentoAnexo.Tipo.NOTA_FISCAL;
            anexo.url = urlNota;
            anexo.persist();
            s.status = SolicitacaoReembolso.Status.AGUARDANDO_PAGAMENTO;
            s.persist();
        }
        return s;
    }

    public List<SolicitacaoReembolso> listarParaAprovacao() {
        return SolicitacaoReembolso.list("status", SolicitacaoReembolso.Status.PENDENTE);
    }

    @Transactional
    public SolicitacaoReembolso aprovarSolicitacao(Long id, String pastorId, String justificativa) {
        SolicitacaoReembolso s = SolicitacaoReembolso.findById(id);
        if (s != null && s.status == SolicitacaoReembolso.Status.PENDENTE) {
            s.status = SolicitacaoReembolso.Status.APROVADA;
            s.persist();
            Aprovacao a = new Aprovacao();
            a.solicitacaoId = id;
            a.aprovadorId = pastorId;
            a.aprovado = true;
            a.justificativa = justificativa;
            a.persist();
        }
        return s;
    }

    @Transactional
    public SolicitacaoReembolso rejeitarSolicitacao(Long id, String pastorId, String justificativa) {
        SolicitacaoReembolso s = SolicitacaoReembolso.findById(id);
        if (s != null && s.status == SolicitacaoReembolso.Status.PENDENTE) {
            s.status = SolicitacaoReembolso.Status.REJEITADA;
            s.persist();
            Aprovacao a = new Aprovacao();
            a.solicitacaoId = id;
            a.aprovadorId = pastorId;
            a.aprovado = false;
            a.justificativa = justificativa;
            a.persist();
        }
        return s;
    }

    public List<SolicitacaoReembolso> listarParaPagamento() {
        return SolicitacaoReembolso.list("status", SolicitacaoReembolso.Status.AGUARDANDO_PAGAMENTO);
    }

    @Transactional
    public SolicitacaoReembolso executarPagamento(Long id, String tesoureiroId, String urlComprovante) {
        SolicitacaoReembolso s = SolicitacaoReembolso.findById(id);
        if (s != null && s.status == SolicitacaoReembolso.Status.AGUARDANDO_PAGAMENTO) {
            Pagamento p = new Pagamento();
            p.solicitacaoId = id;
            p.tesoureiroId = tesoureiroId;
            p.concluido = true;
            p.persist();
            DocumentoAnexo anexo = new DocumentoAnexo();
            anexo.solicitacaoId = id;
            anexo.tipo = DocumentoAnexo.Tipo.COMPROVANTE_PAGAMENTO;
            anexo.url = urlComprovante;
            anexo.persist();
            s.status = SolicitacaoReembolso.Status.CONCLUIDA;
            s.persist();
        }
        return s;
    }

    @Transactional
    public SolicitacaoReembolso rejeitarPagamento(Long id, String tesoureiroId, String justificativa) {
        SolicitacaoReembolso s = SolicitacaoReembolso.findById(id);
        if (s != null && s.status == SolicitacaoReembolso.Status.AGUARDANDO_PAGAMENTO) {
            Pagamento p = new Pagamento();
            p.solicitacaoId = id;
            p.tesoureiroId = tesoureiroId;
            p.concluido = false;
            p.justificativa = justificativa;
            p.persist();
            s.status = SolicitacaoReembolso.Status.REJEITADA;
            s.persist();
        }
        return s;
    }
}
