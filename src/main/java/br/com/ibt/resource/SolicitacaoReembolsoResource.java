package br.com.ibt.resource;

import br.com.ibt.entity.SolicitacaoReembolso;
import br.com.ibt.service.SolicitacaoReembolsoService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/reembolsos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SolicitacaoReembolsoResource {

    @Inject
    SolicitacaoReembolsoService service;

    @POST
    @Transactional
    public Response criar(SolicitacaoReembolso dto) {
        SolicitacaoReembolso criado = service.criarSolicitacao(dto.membroId, dto.descricao, dto.valor);
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @GET
    public List<SolicitacaoReembolso> listar() {
        return service.listarSolicitacoes();
    }

    @PUT
    @Path("/{id}/anexar")
    @Transactional
    public Response anexarNota(@PathParam("id") Long id, DocumentoNota dto) {
        SolicitacaoReembolso s = service.atualizarSolicitacao(id, dto.urlNota, dto.chavePix);
        return Response.ok(s).build();
    }

    @PUT
    @Path("/{id}/status")
    @Transactional
    public Response definirStatus(@PathParam("id") Long id, StatusUpdate dto) {
        SolicitacaoReembolso s = service.definirStatus(id, dto.status, dto.justificativa);
        return Response.ok(s).build();
    }

    @PUT
    @Path("/{id}/pagamento")
    @Transactional
    public Response processarPagamento(@PathParam("id") Long id, PagamentoDTO dto) {
        SolicitacaoReembolso s = service.processarPagamento(id, dto.tesoureiroId, dto.urlComprovante, dto.aprovado);
        return Response.ok(s).build();
    }

    public static class DocumentoNota {
        public String urlNota;
        public String chavePix;
    }

    public static class StatusUpdate {
        public SolicitacaoReembolso.Status status;
        public String justificativa;
    }

    public static class PagamentoDTO {
        public String tesoureiroId;
        public String urlComprovante;
        public boolean aprovado;
    }
}
