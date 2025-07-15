package br.com.ibt.resource;

import br.com.ibt.entity.SolicitacaoReembolso;
import br.com.ibt.service.SolicitacaoReembolsoService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;

@Path("/reembolsos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SolicitacaoReembolsoResource {

    @Inject
    SolicitacaoReembolsoService service;

    @Inject
    JsonWebToken jwt;

    @POST
    @RolesAllowed("Membros")
    @Transactional
    public Response criar(SolicitacaoReembolso dto) {
        Long membroId = Long.valueOf(jwt.getSubject());
        SolicitacaoReembolso criado = service.criarSolicitacao(membroId, dto.descricao, dto.valor);
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @GET
    @RolesAllowed("Membros")
    public List<SolicitacaoReembolso> listarMinhas() {
        Long membroId = Long.valueOf(jwt.getSubject());
        return service.listarMinhasSolicitacoes(membroId);
    }

    @GET
    @RolesAllowed("pastores")
    @Path("/pendentes")
    public List<SolicitacaoReembolso> listarParaAprovacao() {
        return service.listarParaAprovacao();
    }

    @PUT
    @Path("/{id}/aprovar")
    @RolesAllowed("pastores")
    @Transactional
    public Response aprovar(@PathParam("id") Long id, String justificativa) {
        String pastorId = jwt.getSubject();
        SolicitacaoReembolso s = service.aprovarSolicitacao(id, pastorId, justificativa);
        return Response.ok(s).build();
    }

    @PUT
    @Path("/{id}/rejeitar")
    @RolesAllowed("pastores")
    @Transactional
    public Response rejeitar(@PathParam("id") Long id, String justificativa) {
        String pastorId = jwt.getSubject();
        SolicitacaoReembolso s = service.rejeitarSolicitacao(id, pastorId, justificativa);
        return Response.ok(s).build();
    }

    @PUT
    @Path("/{id}/anexar")
    @RolesAllowed("Membros")
    @Transactional
    public Response anexarNota(@PathParam("id") Long id, DocumentoNota dto) {
        SolicitacaoReembolso s = service.anexarNotaFiscal(id, dto.urlNota, dto.chavePix);
        return Response.ok(s).build();
    }

    @GET
    @RolesAllowed("tesoureiros")
    @Path("/pagamento")
    public List<SolicitacaoReembolso> listarParaPagamento() {
        return service.listarParaPagamento();
    }

    @PUT
    @Path("/{id}/concluir")
    @RolesAllowed("tesoureiros")
    @Transactional
    public Response concluir(@PathParam("id") Long id, String urlComprovante) {
        String tesId = jwt.getSubject();
        SolicitacaoReembolso s = service.executarPagamento(id, tesId, urlComprovante);
        return Response.ok(s).build();
    }

    @PUT
    @Path("/{id}/rejeitar-pagamento")
    @RolesAllowed("tesoureiros")
    @Transactional
    public Response rejeitarPagamento(@PathParam("id") Long id, String justificativa) {
        String tesId = jwt.getSubject();
        SolicitacaoReembolso s = service.rejeitarPagamento(id, tesId, justificativa);
        return Response.ok(s).build();
    }

    public static class DocumentoNota {
        public String urlNota;
        public String chavePix;
    }
}
