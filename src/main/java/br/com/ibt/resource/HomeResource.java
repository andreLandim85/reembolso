package br.com.ibt.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/home")
public class HomeResource {
    @GET
    public Response home() {
        // retorne sua página estática ou redirecione para um template Qute
        return Response.ok("Bem-vindo! <a href=\"/oauth2/authorization/cognito\">Login</a>").build();
    }
}