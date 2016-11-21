package cpen442.securefileshare.cpen442.encryption;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

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
        return generateSecureRandom(IV_SIZE);
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
    public static byte[] encrypt(byte[] unencrypted, SecretKey key, byte[] IV) throws InvalidKeyException {
        try {
            return transform(unencrypted, key, IV, Cipher.ENCRYPT_MODE);
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decrypt(byte[] encrypted, SecretKey key) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException{
        byte[] IV = Arrays.copyOf(encrypted, IV_SIZE);
        return transform(encrypted, key, IV, Cipher.DECRYPT_MODE);
    }

    public static byte[] generateSecureRandom(int numberOfBytes) {
        byte[] buff = new byte[numberOfBytes];
        SecureRandom random = new SecureRandom();
        return buff;
    }

    public static byte[] transform(byte[] dataWithIV, SecretKey key, byte[] IV, int opmode) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
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
        return c.doFinal(dataWithIV, IV_SIZE, dataWithIV.length - IV_SIZE);
    }
}
