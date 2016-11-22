package cpen442.securefileshare.encryption;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * Created by Cyberus on 2016-11-21.
 */

public class Utility {


    public static byte[] generateSecureRandom(int numberOfBytes) {
        byte[] buff = new byte[numberOfBytes];
        SecureRandom random = new SecureRandom();
        random.nextBytes(buff);
        return buff;
    }

    public static String toBase64String(byte[] bytes){
        byte[] data = Base64.encode(bytes, Base64.DEFAULT);
        return new String(data, StandardCharsets.UTF_8);
    }

    public static byte[] toBytes(String base64) {
        byte[] data = base64.getBytes(StandardCharsets.UTF_8);
        byte[] retval = Base64.decode(data, Base64.DEFAULT);
        return retval;
    }

}
