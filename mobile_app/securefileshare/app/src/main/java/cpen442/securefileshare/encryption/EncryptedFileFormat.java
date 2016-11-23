package cpen442.securefileshare.encryption;

/**
 * Created by Cyberus on 2016-11-22.
 */

public class EncryptedFileFormat extends StringPlusData{
    public EncryptedFileFormat(){}

    public EncryptedFileFormat(byte[] allBytes) throws FormatException  { super(allBytes); }

    public EncryptedFileFormat(byte[] encryptedBytes, String userId) { super(encryptedBytes, userId); }

    public String getUserId() { return string; }

    public byte[] getEncryptedData(){
        return data;
    }
}
