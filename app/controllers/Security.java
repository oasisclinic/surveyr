package controllers;

import play.mvc.Http.Context;
import play.mvc.Security.Authenticator;
import play.mvc.Result;

/**
 * Author:
 * Bradley H Davis
 * 720326365
 * The University of North Carolina at Chapel Hill
 * Created on 12/1/14 at 3:12 PM.
 */
public class Security extends Authenticator {

    @Override
    public Result onUnauthorized(Context ctx) {
        return JsonResponse
    }

}
