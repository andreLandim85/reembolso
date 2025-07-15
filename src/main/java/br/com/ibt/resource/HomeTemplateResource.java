package br.com.ibt.resource;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/")                            // mapeia o root
public class HomeTemplateResource {

    @Inject
    Template home;                    // injeta o template home.html

    @Inject
    JsonWebToken jwt;

    @GET
    @PermitAll
    @Produces(MediaType.TEXT_HTML)   // diz que vai devolver HTML
    public TemplateInstance home() {
        boolean auth = jwt.getClaimNames() != null && jwt.getSubject() != null;
        String name = auth ? jwt.getName() : null;
        return home
                .data("isAuthenticated", auth)
                .data("userName", name);
    }
}