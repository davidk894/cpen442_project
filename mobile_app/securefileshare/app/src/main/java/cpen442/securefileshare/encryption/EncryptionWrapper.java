package cpen442.securefileshare.encryption;

import java.security.InvalidKeyException;
import javax.crypto.SecretKey;

/**
 * Created by Cyberus on 2016-11-07.
 */

public class EncryptionWrapper {
    public static byte[] encrypt(byte[] data, SecretKey key) throws EncryptionException {
        byte[] IV = Encryption.generateIV();
        byte[] hash = HashByteWrapper.computeHash(data);
        byte[] toEncrypt = new byte[hash.length + data.length];
        System.arraycopy(hash, 0, toEncrypt, 0, hash.length);
        System.arraycopy(data, 0, toEncrypt, hash.length, data.length);
        byte[] toReturn;
        try {
            toReturn = Encryption.encrypt(toEncrypt, key);
        } catch (InvalidKeyException e) {
            throw new EncryptionException("Invalid Key", e);
        }
        return toReturn;
    }

    public static byte[] decrypt(byte[] encrypted, SecretKey key) throws EncryptionException {
        byte[] decrypted;
        try {
            decrypted = Encryption.decrypt(encrypted, key);
        } catch (InvalidKeyException e) {
            throw new EncryptionException("Invalid Key", e);
        } catch (Exception e) {
            throw new EncryptionException("Invalid padding/blockSize.", e);
        }
        HashByteWrapper hbw = new HashByteWrapper(decrypted);
        if (!hbw.doesHashMatch()) {
            throw new EncryptionException("Hash does not match.");
        }
        return hbw.getData();
    }
}
