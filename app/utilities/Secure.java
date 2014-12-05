package utilities;
import controllers.SecurityController;
import errors.UnauthorizedError;
import models.AuthToken;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.codec.CodecSupport;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.util.ByteSource;
import play.Configuration;
import play.Play;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security.Authenticator;

/**
 * Handles encryption and authentication tasks.
 * @author Bradley Davis
 */
public class Secure extends Authenticator {

    private static final Configuration conf = Play.application().configuration();
    private static final byte[] keyBytes = Base64.decode(conf.getString("application.key"));

    /** AES service provided by Apache Shiro */
    private static final AesCipherService cipher = new AesCipherService();

    /**
     * Encrypts a string using AES.
     * @param string the string to encrypt
     * @return the encrypted string
     */
    public static String encrypt(String string) {
        byte[] secretBytes = CodecSupport.toBytes(string);
        ByteSource encrypted = cipher.encrypt(secretBytes, keyBytes);
        return encrypted.toBase64();
    }

    /**
     * Decrypts a string encrypted using AES.
     * @param string the string to decrypt
     * @return the plaintext string
     */
    public static String decrypt(String string) {
        byte[] encryptedBytes = Base64.decode(string);
        ByteSource decrypted = cipher.decrypt(encryptedBytes, keyBytes);
        return CodecSupport.toString(decrypted.getBytes());
    }

    /**
     * Returns the UUID value of the authentication token if it exists and is unexpired
     * @param ctx the context of the HTTP request
     * @return a UUID value or null
     */
    @Override
    public String getUsername(Http.Context ctx) {

        String username = null;
        // Although the RFC says headers are case-insensitive, this is a map of headers and thus must be case-sensitive
        String[] authTokenHeaderValues = ctx.request().headers().get(SecurityController.AUTH_TOKEN_HEADER);

        if ((authTokenHeaderValues != null) && (authTokenHeaderValues.length == 1) && (authTokenHeaderValues[0] != null)) {
            AuthToken t = AuthToken.findOne(authTokenHeaderValues[0]);
            if (t != null && t.isValid()) {
                ctx.args.put("token", t);
                return t.getToken();
            }
        }

        return null;

    }

    @Override
    public Result onUnauthorized(Http.Context ctx) {
        return Rest.error(new UnauthorizedError());
    }

}
