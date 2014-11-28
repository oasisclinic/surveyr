package utilities;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.codec.CodecSupport;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.util.ByteSource;
import play.Configuration;
import play.Play;

/**
 * Handles all attribute-level encryption and decryption using AES-256.
 * @author Bradley Davis
 */
public class Security {

    /** Enables access to application configuration file */
    private static final Configuration conf = Play.application().configuration();

    /** Retrieves key from configuration */
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

}
