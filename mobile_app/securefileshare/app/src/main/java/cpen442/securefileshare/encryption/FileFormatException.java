package cpen442.securefileshare.encryption;

/**
 * Created by Cyberus on 2016-11-07.
 */

public class FileFormatException extends Exception {
    public FileFormatException() {}
    public FileFormatException(String message) {
        super(message);
    }
    public FileFormatException(Throwable cause) {
        super(cause);
    }
    public FileFormatException(String message, Throwable cause) {
        super(message, cause);
    }
    public FileFormatException(String message, Throwable cause,
                               boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
