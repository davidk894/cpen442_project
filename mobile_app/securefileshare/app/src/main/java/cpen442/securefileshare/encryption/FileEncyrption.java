package cpen442.securefileshare.encryption;

import java.io.File;
import java.io.IOException;

import javax.crypto.SecretKey;

/**
 * Created by Cyberus on 2016-11-07.
 */

public class FileEncyrption {

    //returns the key
    public static EncryptedPlusKey EncryptFile(String inputFilePath, byte[] fileBytes, String accountID) throws EncryptionException {
        File f = new File(inputFilePath);
        String fileName = f.getName();
        FileFormat FF = new FileFormat(fileBytes, fileName);
        SecretKey key = Encryption.generateKey();
        byte[] encrypted = EncryptionWrapper.encrypt(FF.toBytes(), key);
        return new EncryptedPlusKey(encrypted, key.getEncoded());
    }

    public static FileFormat DecryptFile(String filePath, byte[] key) throws IOException, EncryptionException, FormatException {
        byte[] encryptedFileBytes = FileIO.ReadAllBytes(filePath);
        byte[] decryptedBytes = EncryptionWrapper.decrypt(encryptedFileBytes, Encryption.toKey(key));
        return new FileFormat(decryptedBytes);
    }
}
