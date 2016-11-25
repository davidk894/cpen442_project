package cpen442.securefileshare;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

import cpen442.securefileshare.encryption.Utility;


public class KeyStoreInterface {
    private static final String AKeyStore_String = "AndroidKeyStore";
    private static final String KEY_NAME = "FingerPrintKey12345";
    private static final String cipherMode = "AES/ECB/NoPadding";
    private static final int MESSAGE_SIZE = 16;
    private static final boolean invalidatedByBiometricEnrollment = true;

    public static void generateKey() {
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance(AKeyStore_String);
        } catch (Exception e) {
            e.printStackTrace();
        }
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, AKeyStore_String);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        try {
            keyStore.load(null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(KEY_NAME,
                KeyProperties.PURPOSE_DECRYPT | KeyProperties.PURPOSE_ENCRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                .setUserAuthenticationRequired(true)
                .setRandomizedEncryptionRequired(false)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment);
        }
        try {
            keyGenerator.init(builder.build());
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        keyGenerator.generateKey();
    }

    public static Cipher cipherInit(int mode) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(cipherMode);
            KeyStore keyStore = KeyStore.getInstance(AKeyStore_String);
            keyStore.load(null);
            cipher.init(mode, keyStore.getKey(KEY_NAME, null));
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IOException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException | KeyStoreException | CertificateException e) {
            e.printStackTrace();
        }
        return cipher;
    }

    public static boolean keyExists() {
        try {
            KeyStore keyStore = KeyStore.getInstance(AKeyStore_String);
            keyStore.load(null);
            return keyStore.containsAlias(KEY_NAME);
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void removeKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance(AKeyStore_String);
            keyStore.load(null);
            keyStore.deleteEntry(KEY_NAME);
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            e.printStackTrace();
        }
    }

    public static byte[] transform(Cipher cipher, byte[] toTransform) {
        if (toTransform.length == MESSAGE_SIZE) {
            try {
                return cipher.doFinal(toTransform);
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String generateCryptoMessage() {
        return generateSecureRandomString(MESSAGE_SIZE);
    }

    private static String generateSecureRandomString(int numberOfBytes) {
        return Utility.toBase64String(Utility.generateSecureRandom(numberOfBytes));
    }
}
