package br.com.ibt.service;

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

    public List<SolicitacaoReembolso> listarSolicitacoes() {
        return SolicitacaoReembolso.listAll();
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
            s.persist();
        }
        return s;
    }

    @Transactional
    public SolicitacaoReembolso definirStatus(Long id, SolicitacaoReembolso.Status status, String justificativa) {
        SolicitacaoReembolso s = SolicitacaoReembolso.findById(id);
        if (s != null) {
            s.status = status;
            s.persist();
        }
        return s;
    }

    @Transactional
    public SolicitacaoReembolso processarPagamento(Long id, String tesoureiroId, String urlComprovante, boolean aprovado) {
        SolicitacaoReembolso s = SolicitacaoReembolso.findById(id);
        if (s != null && s.status == SolicitacaoReembolso.Status.AGUARDANDO_PAGAMENTO) {
            Pagamento p = new Pagamento();
            p.solicitacaoId = id;
            p.tesoureiroId = tesoureiroId;
            p.concluido = aprovado;
            p.justificativa = aprovado ? null : "Rejeitado";
            p.persist();
            DocumentoAnexo anexo = new DocumentoAnexo();
            anexo.solicitacaoId = id;
            anexo.tipo = aprovado ? DocumentoAnexo.Tipo.COMPROVANTE_PAGAMENTO : DocumentoAnexo.Tipo.NOTA_FISCAL;
            anexo.url = urlComprovante;
            anexo.persist();
            s.status = aprovado ? SolicitacaoReembolso.Status.CONCLUIDA : SolicitacaoReembolso.Status.REJEITADA;
            s.persist();
        }
        return s;
    }
}