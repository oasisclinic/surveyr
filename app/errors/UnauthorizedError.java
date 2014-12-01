package errors;

import play.Configuration;
import play.Play;

/**
 * Occurs when a unauthorized request is made.
 * @author Bradley Davis
 */
public class UnauthorizedError implements RestError {

    private static final Configuration conf = Play.application().configuration();
    private static final String UNAUTHORIZED = conf.getString("errors.unauthorized");

    @Override
    public int getStatusCode() {
        return 401;
    }

    @Override
    public String getMessage() {
        return UNAUTHORIZED;
    }

}
