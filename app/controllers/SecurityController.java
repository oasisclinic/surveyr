package controllers;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import errors.EmptyResponseBodyException;
import models.AuthToken;
import models.dto.SurveyDTO;
import play.Configuration;
import play.Play;
import play.mvc.*;
import play.mvc.Security;
import utilities.Rest;
import utilities.Secure;

import javax.ws.rs.Produces;

/**
 * Handles authentication operations including issuing tokens and expiring them.
 * @author Bradley Davis
 */
@Api(value = "/api/security", description = "Handles authentication operations")
public class SecurityController extends Controller {

    private static final Configuration conf = Play.application().configuration();
    public final static String AUTH_TOKEN_HEADER = conf.getString("auth.header");
    public static final String AUTH_TOKEN = conf.getString("auth.cookie");

    /**
     * Generates an authentication token for use in subsequent request headers
     * TODO: implement rules for issuing tokens, i.e. username and password matching
     * @return an authentication token object
     */
    @ApiOperation(nickname = "authenticate", value = "Issue authentication token", httpMethod = "GET")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Token issued"),
            @ApiResponse(code = 401, message = "Unauthorized")
    })
    @Produces("application/json")
    public static Result authenticate() {

        AuthToken t = new AuthToken().save();
        response().setCookie(AUTH_TOKEN, t.getToken());
        try {
            return Rest.json(t);
        } catch (EmptyResponseBodyException e) {
            return internalServerError();
        }

    }

    /**
     * Destroys an authentication token. Token must be valid in order to destroy it.
     * @return HTTP response
     */
    @ApiOperation(nickname = "expire", value = "Expire an authentication token", httpMethod = "GET")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Acknowledged"),
            @ApiResponse(code = 401, message = "Unauthorized")
    })
    @Produces("application/json")
    @Security.Authenticated(Secure.class)
    public static Result expire() {

        AuthToken token = (AuthToken)Http.Context.current().args.get("token");
        token.remove();
        response().discardCookie(AUTH_TOKEN);
        return redirect("/");

    }

}