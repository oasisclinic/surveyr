package errors;

import play.Configuration;
import play.Play;

/**
 * Occurs when no patients could be found using the provided parameters.
 * @author Bradley Davis
 */
public class NoPatientsFoundError implements RestError {

    private static final Configuration conf = Play.application().configuration();
    private static final String NO_PATIENTS_FOUND = conf.getString("errors.nopatientsfound");

    @Override
    public int getStatusCode() {
        return 404;
    }

    @Override
    public String getMessage() {
        return NO_PATIENTS_FOUND;
    }

}
