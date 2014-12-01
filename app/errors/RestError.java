package errors;

/**
 * Defines a common signature for errors to REST responses.
 * @author Bradley Davis
 */
public interface RestError {

    /**
     * Gets the HTTP status code that corresponds to the error.
     * @return the status code
     */
    public int getStatusCode();

    /**
     * Produces an user-friendly error message
     * @return the error message
     */
    public String getMessage();

}
