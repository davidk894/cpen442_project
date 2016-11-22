package cpen442.securefileshare.encryption;

/**
 * Created by Cyberus on 2016-11-07.
 */

public class EncryptionException extends Exception {
    public EncryptionException() {}
    public EncryptionException(String message) {
        super(message);
    }
    public EncryptionException(Throwable cause) {
        super(cause);
    }
    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
    public EncryptionException(String message, Throwable cause,
                               boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
