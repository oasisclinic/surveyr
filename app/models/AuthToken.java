package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import play.Configuration;
import play.Play;
import uk.co.panaxiom.playjongo.PlayJongo;

import java.util.Date;
import java.util.UUID;

/**
 * Encapsulates an authentication token and provides expiration data.
 * @author Bradley Davis
 */
public class AuthToken {

    private static final Configuration conf = Play.application().configuration();
    private static final int TOKEN_TIMEOUT = conf.getInt("auth.timeout");
    private static final long ONE_MINUTE_MS = 60000;

    private static final MongoCollection collection = PlayJongo.getCollection("tokens");

    private ObjectId _id;
    private String token;
    private Date expirationDate;

    /**
     * Creates a new AuthToken by generating a UUID token and setting the expiration date in minutes.
     */
    public AuthToken() {
        this.token = UUID.randomUUID().toString();
        this.expirationDate = new Date(new Date().getTime() + (TOKEN_TIMEOUT * ONE_MINUTE_MS));
    }

    /**
     * Checks if a token is still valid
     * @return false if the current date is after the token's expiration date
     */
    @JsonIgnore
    public boolean isValid() {
        if (new Date().after(this.expirationDate)) {
            this.remove();
            return false;
        }
        else return true;
    }

    /**
     * Saves a token.
     * @return the token that was saved
     */
    public AuthToken save() {
        collection.save(this);
        return this;
    }

    /**
     * Finds a single token based on its UUID value
     * @param token the token to find
     * @return the token
     */
    public static AuthToken findOne(String token) {
        return collection.findOne("{token: #}", token).as(AuthToken.class);
    }

    /**
     * Discards a token.
     */
    public void remove() {
        collection.remove(this._id);
    }

    public String getToken() {
        return token;
    }

}
