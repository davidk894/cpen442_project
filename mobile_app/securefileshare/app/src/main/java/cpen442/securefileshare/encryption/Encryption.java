package cpen442.securefileshare.encryption;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.SynchronousQueue;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Cyberus on 2016-11-06.
 */

public class Encryption {
    private static String cipherMode = "AES/CBC/PKCS5Padding";
    private static String algoritm = "AES";
    private static int IV_SIZE = 16;
    private static int KEY_SIZE = 128;


    public static byte[] generateIV()
    {
        return Utility.generateSecureRandom(IV_SIZE);
    }

    public static SecretKey generateKey() {
        KeyGenerator kg = null;
        try {
            kg = KeyGenerator.getInstance(algoritm);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        kg.init(KEY_SIZE);
        return kg.generateKey();
    }

    public static SecretKey toKey(byte[] key) {
        return new SecretKeySpec(key, 0, key.length, algoritm);
    }

    //the unencrypted should have the IV prepended to it; for efficiency
    public static byte[] encrypt(byte[] unencrypted, SecretKey key) throws InvalidKeyException {
        try {
            byte[] IV = generateIV();
            byte[] encrypted = transform(unencrypted, key, IV, Cipher.ENCRYPT_MODE);
            byte[] ivPlusEncrypted = new byte[IV.length + encrypted.length];
            System.arraycopy(IV, 0, ivPlusEncrypted, 0, IV.length);
            System.arraycopy(encrypted, 0, ivPlusEncrypted, IV.length, ivPlusEncrypted.length);
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decrypt(byte[] encrypted, SecretKey key) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException{
        byte[] IV = Arrays.copyOf(encrypted, IV_SIZE);
        byte[] toTransform  = Arrays.copyOfRange(encrypted, IV_SIZE, encrypted.length);
        return transform(toTransform, key, IV, Cipher.DECRYPT_MODE);
    }

    public static byte[] transform(byte[] data, SecretKey key, byte[] IV, int opmode) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher c = null;
        try {
            c = Cipher.getInstance(cipherMode);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
        IvParameterSpec ivPar = new IvParameterSpec(IV);
        try {
            c.init(opmode, key, ivPar);
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return c.doFinal(data);
    }
}
