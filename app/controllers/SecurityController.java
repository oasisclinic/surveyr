package controllers;

import errors.EmptyResponseBodyException;
import models.AuthToken;
import play.Configuration;
import play.Play;
import play.mvc.*;
import play.mvc.Security;
import utilities.RestResponse;
import utilities.Secure;

/**
 * Handles login and logout tasks.
 * @author Bradley Davis
 */
public class SecurityController extends Controller {

    private static final Configuration conf = Play.application().configuration();
    public final static String AUTH_TOKEN_HEADER = conf.getString("auth.header");
    public static final String AUTH_TOKEN = conf.getString("auth.cookie");

    /**
     * Generates an authentication token for use in subsequent requests
     * @return an authentication token object
     */
    public static Result login() {

        AuthToken t = new AuthToken().save();
        response().setCookie(AUTH_TOKEN, t.getToken());
        try {
            return RestResponse.json(t);
        } catch (EmptyResponseBodyException e) {
            return internalServerError();
        }

    }

    /**
     * Destroys an authentication token. Token must be valid in order to destroy it.
     * @return HTTP response
     */
    @Security.Authenticated(Secure.class)
    public static Result logout() {

        AuthToken token = (AuthToken)Http.Context.current().args.get("token");
        token.remove();
        response().discardCookie(AUTH_TOKEN);
        return redirect("/");

    }

}
