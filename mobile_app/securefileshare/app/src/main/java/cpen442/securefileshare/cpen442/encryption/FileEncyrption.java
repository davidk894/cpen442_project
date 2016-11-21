package cpen442.securefileshare.cpen442.encryption;

import java.io.IOException;

import javax.crypto.SecretKey;

/**
 * Created by Cyberus on 2016-11-07.
 */

public class FileEncyrption {

    //returns the key
    public byte[] EncryptFile(String inputFileFolder, String inputFileName, String outputFileFolder, String outputFileName) throws IOException, EncryptionException {
        String inputFilePath = FileIO.combine(inputFileFolder, inputFileName);
        byte[] fileBytes = FileIO.ReadAllBytes(inputFilePath);
        FileFormat FF = new FileFormat(fileBytes, inputFileName);
        SecretKey key = Encryption.generateKey();
        byte[] encrypted = EncryptionWrapper.encrypt(FF.toBytes(), key);
        String outputFilePath = FileIO.combine(outputFileFolder, outputFileName);
        FileIO.WriteAllBytes(outputFilePath, encrypted);
        return key.getEncoded();
    }

    public FileFormat DecryptFile(String filePath, byte[] key) throws IOException, EncryptionException, FileFormatException {
        byte[] encryptedFileBytes = FileIO.ReadAllBytes(filePath);
        byte[] decryptedBytes = EncryptionWrapper.decrypt(encryptedFileBytes, Encryption.toKey(key));
        return new FileFormat(decryptedBytes);
    }
}
