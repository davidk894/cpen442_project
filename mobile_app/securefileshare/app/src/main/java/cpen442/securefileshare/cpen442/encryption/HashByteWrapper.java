package cpen442.securefileshare.cpen442.encryption;

import android.renderscript.ScriptIntrinsicYuvToRGB;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by Cyberus on 2016-11-07.
 */

//byte array format -> hash || data
public class HashByteWrapper {
    private static final String HASH_FORMAT = "SHA-256";
    private static final int LENGTH_OF_HASH = 32;

    private byte[] hash;
    private byte[] data;

    public HashByteWrapper(byte[] allBytes) {
        hash = Arrays.copyOf(allBytes, LENGTH_OF_HASH);
        data = Arrays.copyOfRange(allBytes, LENGTH_OF_HASH, allBytes.length);
    }

    public boolean doesHashMatch() {
        return Arrays.equals(hash, computeHash(data));
    }

    public static byte[] computeHash(byte[] toHash) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(HASH_FORMAT);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return md.digest(toHash);
    }

    public byte[] getData() {
        return data;
    }
}

