package utilities;

import errors.EmptyResponseBodyException;
import errors.RestError;
import play.Configuration;
import play.Logger;
import play.Play;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Collection;

/**
 * Handles all rest responses, including CORS preflight requests and response serialization.
 * @author Bradley Davis
 */
public class RestResponse extends Controller {

    // Load CORS header preferences from application configuration
    private static final Configuration conf = Play.application().configuration();
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN = conf.getString("cors.access-control-allow-origin");
    private static final String ACCESS_CONTROL_ALLOW_METHODS = conf.getString("cors.access-control-allow-methods");
    private static final String ACCESS_CONTROL_ALLOW_HEADERS = conf.getString("cors.access-control-allow-headers");

    /**
     * Responds to OPTIONS requests on the API by adding the appropriate CORS headers
     * @param path wildcard string representing the entire path of the request
     * @return HTTP 200 OK with appropriate CORS headers
     */
    public static Result options(Object path) {

        response().setContentType("application/json");
        response().setHeader("Access-Control-Allow-Origin", ACCESS_CONTROL_ALLOW_ORIGIN);
        response().setHeader("Access-Control-Allow-Methods", ACCESS_CONTROL_ALLOW_METHODS);
        response().setHeader("Access-Control-Allow-Headers", ACCESS_CONTROL_ALLOW_HEADERS);
        return ok();

    }

    /**
     * Sends a HTTP response using a RestError object
     * @param obj the RestError to send
     * @return HTTP response
     */
    public static <T extends RestError> Result error(T obj) {

        try {
            return json(obj.getStatusCode(), obj);
        } catch (EmptyResponseBodyException e) {
            Logger.trace("REST error encountered with empty error object, returning HTTP 500");
            return internalServerError();
        }

    }

    /**
     * Sends a HTTP response using a status code of 200 and serializes an object into JSON for the response body
     * @param obj the object to serialize to JSON
     * @return HTTP response
     * @throws EmptyResponseBodyException if the object is null
     */
    public static Result json(Object obj) throws EmptyResponseBodyException {

        return json(200, obj);

    }

    /**
     * Sends a HTTP response using a status code and serializes an object into JSON for the response body
     * @param httpCode the status code to respond with
     * @param obj the object to serialize to JSON
     * @return HTTP response
     * @throws EmptyResponseBodyException if the object is null
     */
    public static Result json(int httpCode, Object obj) throws EmptyResponseBodyException {

        if (obj == null) throw new EmptyResponseBodyException();
        else if (obj instanceof Collection) {
            if (((Collection) obj).size() == 0) throw new EmptyResponseBodyException();
        }
        return status(httpCode, Json.toJson(obj));

    }

}
