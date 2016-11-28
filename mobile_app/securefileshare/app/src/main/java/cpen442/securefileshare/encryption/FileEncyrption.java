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
        EncryptedFileFormat eff = new EncryptedFileFormat(encrypted, accountID);
        return new EncryptedPlusKey(eff, key.getEncoded());
    }

    public static FileFormat DecryptFile(byte[] encryptedFileBytes, byte[] key) throws EncryptionException, FormatException {
        EncryptedFileFormat eff = new EncryptedFileFormat(encryptedFileBytes);
        byte[] decryptedBytes = EncryptionWrapper.decrypt(eff.getEncryptedData(), Encryption.toKey(key));
        return new FileFormat(decryptedBytes);
    }
}
