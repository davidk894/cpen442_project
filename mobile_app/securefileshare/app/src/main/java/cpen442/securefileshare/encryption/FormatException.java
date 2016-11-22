package cpen442.securefileshare.encryption;

/**
 * Created by Cyberus on 2016-11-07.
 */

public class FormatException extends Exception {
    public FormatException() {}
    public FormatException(String message) {
        super(message);
    }
    public FormatException(Throwable cause) {
        super(cause);
    }
    public FormatException(String message, Throwable cause) {
        super(message, cause);
    }
    public FormatException(String message, Throwable cause,
                           boolean enableSuppression, boolean writableStackTrace) {
        //super(message, cause, enableSuppression, writableStackTrace);
    }
}
