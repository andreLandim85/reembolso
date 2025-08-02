package br.com.ibt.resource;

import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.*;
import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/")
public class LoginRedirectResource {

    @Inject
    Template home;

    @Inject
    SecurityIdentity identity;

    @GET
    @Path("/home")
    @Authenticated
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance home() {
        boolean isMembro      = identity.hasRole("Membro");
        boolean isPastor      = identity.hasRole("Pastores");
        boolean isTesoureiro  = identity.hasRole("Tesoureiros");
        boolean isAdmin  = identity.hasRole("Admin");

        var principal = (OidcJwtCallerPrincipal) identity.getPrincipal();
        String username = principal.getClaim("name");
        String celular = principal.getClaim("phone_number");
        String email = principal.getClaim("email");
        System.out.println("celular: " + celular+ "; email: " + email);
        return home
                .data("isMembro", isMembro)
                .data("isPastor", isPastor)
                .data("isTesoureiro", isTesoureiro)
                .data("username", username);
    }

    @GET
    @Path("/cognito-logout")
    public Response logout(@Context UriInfo uriInfo) {
        String clientId = "2cdjagd9aavp7f45n7is71ntvn";
        String cognitoDomain = "us-east-1udo9sf2dy.auth.us-east-1.amazoncognito.com";
        String redirectUri = uriInfo.getBaseUri().toString().concat("home");

        String cognitoLogoutUrl = String.format(
                "https://%s/logout?client_id=%s&logout_uri=%s",
                cognitoDomain, clientId, redirectUri
        );
        // Redireciona para o logout do Cognito
        return Response.seeOther(URI.create(cognitoLogoutUrl)).build();
    }


    @GET
    @Path("/debug/headers")
    @Produces(MediaType.TEXT_PLAIN)
    public String showHeaders(@Context HttpHeaders headers) {
        return headers.getRequestHeaders().entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("\n"));
    }
}
