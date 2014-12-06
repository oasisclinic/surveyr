import errors.PlayError;
import play.GlobalSettings;
import play.api.mvc.EssentialFilter;
import play.filters.gzip.GzipFilter;
import play.filters.headers.SecurityHeadersFilter;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.SimpleResult;
import utilities.Rest;

import static javax.ws.rs.core.Response.ok;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.internalServerError;
import static play.mvc.Results.notFound;

/**
 * Enables critical filters for all requests and handles errors
 * @author Bradley Davis
 */
public class Global extends GlobalSettings {

    public <T extends EssentialFilter> Class<T>[] filters() {
        return new Class[]{SecurityHeadersFilter.class, GzipFilter.class};
    }

    public F.Promise<Result> onError(Http.RequestHeader request, Throwable t) {
        return F.Promise.<Result>pure(Rest.error(new PlayError(t.getMessage(), 500)));
    }

    public F.Promise<Result> onBadRequest(Http.RequestHeader request, String error) {
        return F.Promise.<Result>pure(Rest.error(new PlayError(error, 400)));
    }

    public F.Promise<Result> onHandlerNotFound(Http.RequestHeader request) {
        return F.Promise.<Result>pure(Rest.error(new PlayError("Invalid URL", 404)));
    }

}