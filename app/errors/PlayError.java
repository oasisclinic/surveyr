package errors;

import play.Configuration;
import play.Play;

public class PlayError implements RestError {

    private static final Configuration conf = Play.application().configuration();
    private static final String MESSAGE = conf.getString("errors.playerror");
    private String error;
    private int status;

    public PlayError(String error, int status) {
        this.error = error;
        this.status = status;
    }

    @Override
    public int getStatusCode() {
        return status;
    }

    @Override
    public String getMessage() {
        return String.format(MESSAGE, error);
    }

}
