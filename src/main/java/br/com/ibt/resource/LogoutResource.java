package br.com.ibt.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.net.URI;

@Path("/logout")
public class LogoutResource {

    private static final String COGNITO_DOMAIN = "https://us-east-1udo9sf2dy.auth.us-east-1.amazoncognito.com";
    private static final String CLIENT_ID = "2cdjagd9aavp7f45n7is71ntvn";
    private static final String LOGOUT_REDIRECT = "https://d84l1y8p4kdic.cloudfront.net";

    @GET
    @RolesAllowed({ "Membros", "pastores", "tesoureiros", "admin" })
    public Response logout() {
        String target = COGNITO_DOMAIN + "/logout"
                + "?client_id=" + CLIENT_ID
                + "&logout_uri=" + LOGOUT_REDIRECT;
        return Response.seeOther(URI.create(target)).build();
    }
}