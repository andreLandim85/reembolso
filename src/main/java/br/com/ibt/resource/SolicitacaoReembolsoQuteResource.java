package br.com.ibt.resource;

import br.com.ibt.entity.Membro;
import br.com.ibt.entity.SolicitacaoReembolso;
import br.com.ibt.service.SolicitacaoReembolsoService;
import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.nio.file.Files;

import java.util.List;

@Blocking
@Path("/solicitar-reembolso")
public class SolicitacaoReembolsoQuteResource {

    @Inject
    Template solicitarReembolso;
    @Inject
    Template aprovarRejeitarSolicitacao;
    @Inject
    Template efetivarPagamento;

    @Inject
    SecurityIdentity identity;


    @Inject
    SolicitacaoReembolsoService service;


    @RolesAllowed({"Membro", "Admin"})
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        Membro m = obterMembro();
        List<SolicitacaoReembolso> solicitacoes = service.listarUltimasPorMembro(m.id);
        return solicitarReembolso
                .data("username", m.nome)
                .data("solicitacoes", solicitacoes)
                .data("mensagem", null)
                .data("descricaoPreenchida", null)
                .data("valorPreenchido", null);
    }



    @POST
    @RolesAllowed({"Membro", "Admin"})
    @Transactional
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public TemplateInstance post(@FormParam("membroId") Long membroId,
                                 @FormParam("descricao") String descricao,
                                 @FormParam("valor") String valor) {
        try {
            Membro m = obterMembro();
            double valorNum = Double.parseDouble(valor.replace(',', '.'));
            var principal = (OidcJwtCallerPrincipal) identity.getPrincipal();
            String nome = principal.getClaim("name");
            String telefone = principal.getClaim("phone_number");
            String email = principal.getClaim("email");


            service.criarSolicitacao(descricao, valorNum, nome, telefone, email);
            List<SolicitacaoReembolso> solicitacoes = service.listarUltimasPorMembro(membroId);
            return solicitarReembolso
                    .data("username", m.nome)
                    .data("solicitacoes", solicitacoes)
                    .data("mensagem", "Solicitação enviada com sucesso!")
                    .data("descricaoPreenchida", "")
                    .data("valorPreenchido", "");
        } catch (Exception ex) {
            Membro m = obterMembro();
            List<SolicitacaoReembolso> solicitacoes = service.listarUltimasPorMembro(membroId);
            return solicitarReembolso

                    .data("username", m.nome)
                    .data("solicitacoes", solicitacoes)
                    .data("mensagem", "Erro ao enviar: " + ex.getMessage())
                    .data("descricaoPreenchida", descricao)
                    .data("valorPreenchido", valor);
        }
    }

    @GET
    @Path("/pendentes")
    @Blocking
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance listarPendentes() {
        // Lista com objetos contendo: nomeMembro, descricao, valor, id
        List<SolicitacaoReembolso> pendentes = service.listarPendentes();
        return aprovarRejeitarSolicitacao.data("pendentes", pendentes)
                .data("username", obterNomeMembro())
                .data("msg", null)
                .data("erro", null)
                .data("rejeitarId", null);
    }

    @POST
    @Path("/aprovar")
    @Blocking
    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance aprovar(@FormParam("id") Long id) {
        Membro m = obterMembro();
        service.definirStatus(id, SolicitacaoReembolso.Status.APROVADA,null, m);
        // Após aprovar, volta pra tela de pendentes
        List<SolicitacaoReembolso> pendentes = service.listarPendentes();
        return aprovarRejeitarSolicitacao.data("pendentes", pendentes)
                .data("username", obterNomeMembro())
                .data("msg", "Solicitação aprovada!")
                .data("erro", null)
                .data("rejeitarId", null);
    }

    @POST
    @Path("/rejeitar")
    @Blocking
    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance rejeitar(@FormParam("id") Long id, @FormParam("justificativa") String justificativa) {
        if (justificativa == null || justificativa.trim().isEmpty()) {
            List<SolicitacaoReembolso> pendentes = service.listarPendentes();
            return aprovarRejeitarSolicitacao.data("pendentes", pendentes)
                    .data("username", obterNomeMembro())
                    .data("erro", "Justificativa obrigatória para rejeição.")
                    .data("rejeitarId", id)
                    .data("msg", null);
        }
        Membro m = obterMembro();
        service.definirStatus(id, SolicitacaoReembolso.Status.REJEITADA,justificativa, m);
        List<SolicitacaoReembolso> pendentes = service.listarPendentes();
        return aprovarRejeitarSolicitacao.data("pendentes", pendentes)
                .data("username", obterNomeMembro())
                .data("msg", "Solicitação rejeitada.")
                .data("erro", null)
                .data("rejeitarId", null);
    }
    @GET
    @Path("/listar-aprovadas")
    @Produces(MediaType.TEXT_HTML)
    @Blocking
    public TemplateInstance getAguardandoPagamento() {
        // Busca as solicitações do banco (ou use o mock)
        List<SolicitacaoReembolso> aguardandoAprovacao = service.listarAguardandoPagamento();

        // Você pode passar outros dados, como msg, erro, rejeitarId se necessário
        return efetivarPagamento
                .data("aguardandoAprovacao", aguardandoAprovacao)
                .data("username", obterNomeMembro())
                .data("msg", null) // ou algum texto de sucesso, se houver
                .data("erro", null) // ou algum texto de erro, se houver
                .data("rejeitarId", null); // para controle de exibição da justificativa
    }

    @POST
    @Path("/{id}/anexar")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    public Response anexarNota(
            @PathParam("id") Long id,
            @RestForm("notaFiscal") FileUpload notaFiscal,
            @RestForm("chavePix") String chavePix,
            @RestForm("tipoChavePix") String tipoChavePix
    ) {
        // Validação do arquivo
        if (notaFiscal == null || notaFiscal.fileName() == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Arquivo obrigatório.").build();
        }
        String fileName = notaFiscal.fileName().toLowerCase();
        String mimeType = notaFiscal.contentType();
        if (!(fileName.endsWith(".pdf") || fileName.endsWith(".png")) ||
                !(mimeType.equals("application/pdf") || mimeType.equals("image/png"))) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Arquivo deve ser PDF ou PNG.").build();
        }

        // Validação da chave Pix
        if (chavePix == null || chavePix.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Chave Pix obrigatória.").build();
        }

        // Ler o arquivo como bytes
        byte[] arquivoBytes;
        try {
            arquivoBytes = java.nio.file.Files.readAllBytes(notaFiscal.uploadedFile());
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Falha ao ler arquivo.").build();
        }

        // Persistir no banco
        service.anexarNotaFiscal(
                id,
                arquivoBytes,
                notaFiscal.fileName(),
                notaFiscal.contentType(),
                chavePix,
                tipoChavePix
        );

        // Redirecionamento
        return Response
                .seeOther(java.net.URI.create("/solicitar-reembolso"))
                .build();
    }

    @GET
    @Path("{id}/download-anexo")
    @Produces({"application/pdf", "image/png"})
    public Response downloadAnexo(@PathParam("id") Long id) {
        SolicitacaoReembolso s = service.buscarPorId(id);
        if (s == null || s.notaFiscal == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        String fileName = s.notaFiscalNome != null ? s.notaFiscalNome : "anexo";
        String mimeType = s.notaFiscalTipo != null ? s.notaFiscalTipo : "application/octet-stream";
        return Response.ok(s.notaFiscal)
                .type(mimeType)
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .build();
    }

    private Membro obterMembro() {
        var principal = (OidcJwtCallerPrincipal) identity.getPrincipal();
        String nome = principal.getClaim("name");
        String telefone = principal.getClaim("phone_number");
        String email = principal.getClaim("email");

        return service.getMembro(nome, telefone, email);
    }

    private String obterNomeMembro() {
        Membro m = obterMembro();
        return m.nome;
    }

    @POST
    @Path("/{id}/executar")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    public Response executarPagamento(
            @PathParam("id") Long id) {

        SolicitacaoReembolso s = service.buscarPorId(id);
        if (s == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        Membro m = obterMembro();
        service.processarPagamento(id, m, SolicitacaoReembolso.Status.CONCLUIDA);

        // Redirecionamento
        return Response
                .seeOther(java.net.URI.create("/solicitar-reembolso"))
                .build();
    }
}
