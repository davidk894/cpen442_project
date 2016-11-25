package cpen442.securefileshare.encryption;

/**
 * Created by Cyberus on 2016-11-21.
 */

public class EncryptedPlusKey {
    public EncryptedFileFormat encryptedFile;
    public byte[] key;

    public EncryptedPlusKey(){}
    public EncryptedPlusKey(EncryptedFileFormat encryptedFile, byte[] key){
        this.encryptedFile = encryptedFile;
        this.key = key;
    }

}
