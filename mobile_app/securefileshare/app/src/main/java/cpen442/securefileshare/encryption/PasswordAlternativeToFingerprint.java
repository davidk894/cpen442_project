package cpen442.securefileshare.encryption;

import java.nio.charset.StandardCharsets;

/**
 * Created by Cyberus on 2016-11-22.
 */

public class PasswordAlternativeToFingerprint {
    public static final int MessageSize = 16;

    //returns a 16 byte value that coresponds to the salt and password
    public static byte[] createKeyFromPass(String salt_base64, String Password) {
        byte[] salt = Utility.toBytes(salt_base64);
        byte[] passwordBytes = Password.getBytes(StandardCharsets.UTF_8);
        //combine
        byte[] combined = new byte[salt.length + passwordBytes.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(passwordBytes, 0, combined, salt.length, passwordBytes.length);
        //hash
        byte[] hash = HashByteWrapper.computeHash(combined);
        //trim
        byte[] toReturn = new byte[MessageSize];
        System.arraycopy(hash, 0, toReturn, 0, toReturn.length);
        return toReturn;
    }

    public static byte[] XorBytes(byte[] bytes1, byte[] bytes2) {
        if ( bytes1.length != MessageSize || bytes2.length != MessageSize) {
            return null;
        }
        byte[] toReturn = new byte[MessageSize];
        for (int i = 0; i < MessageSize; i++) {
            toReturn[i] = (byte) ((bytes1[i]) ^ (bytes2[i]));
        }
        return toReturn;
    }

    public static String generateSalt(int size_bytes){
        return Utility.toBase64String(Utility.generateSecureRandom(size_bytes));
    }

    public static String transform(String random, String salt, String Password) {
        return Utility.toBase64String(
                XorBytes(Utility.toBytes(random), createKeyFromPass(salt, Password)));
    }

}
