package errors;

import play.Configuration;
import play.Play;

/**
 * Occurs when no patients could be found using the provided parameters.
 * @author Bradley Davis
 */
public class NoObjectsFoundError implements RestError {

    private static final Configuration conf = Play.application().configuration();
    private static final String NO_OBJECTS_FOUND = conf.getString("errors.noobjectsfound");
    private String name;

    public NoObjectsFoundError(String name) {
        this.name = name;
    }

    @Override
    public int getStatusCode() {
        return 404;
    }

    @Override
    public String getMessage() {
        return String.format(NO_OBJECTS_FOUND, name);
    }

}
