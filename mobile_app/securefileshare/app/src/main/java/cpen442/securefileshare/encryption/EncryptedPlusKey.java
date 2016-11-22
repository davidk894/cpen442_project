package cpen442.securefileshare.encryption;

/**
 * Created by Cyberus on 2016-11-21.
 */

public class EncryptedPlusKey {
    public byte[] encryptedFile;
    public byte[] key;

    public EncryptedPlusKey(){}
    public EncryptedPlusKey(byte[] file, byte[] key){
        this.encryptedFile = file;
        this.key = key;
    }

}
