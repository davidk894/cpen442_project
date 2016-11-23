package cpen442.securefileshare;

/**
 * Created by Cyberus on 2016-11-21.
 */


public class FileAccessPermision {
    public String filePath;
    public byte[] fileData;
    public byte[] key;
    public String targetID;
    public Purpose purpose;
    public enum Purpose {
        Encrypt_read,
        Encrypt_write,
        Decrypt_read,
        toDecrypt_write,
        toDecrypt_read,
        Decrypt_write
    }

    public FileAccessPermision() {}
}
