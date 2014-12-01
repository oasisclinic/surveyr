package errors;

import play.Configuration;
import play.Play;

/**
 * Occurs when a parameter is of an invalid type or out of bounds.
 * @author Bradley Davis
 */
public class InvalidParameterError implements RestError {

    private static final Configuration conf = Play.application().configuration();
    private static final String INVALID_PARAMETER = conf.getString("invalidparameter");

    @Override
    public int getStatusCode() {
        return 400;
    }

    @Override
    public String getMessage() {
        return INVALID_PARAMETER;
    }

}
