package cpen442.securefileshare;

import org.junit.Test;

import java.security.KeyStoreException;

/**
 * Created by Cyberus on 2016-11-21.
 */

public class KeyStoreInterfaceTest {
    @Test
    public void Test64Conversion(){
        String base64 = KeyStoreInterface.generateCryptoMessage();
        byte[] bytes = KeyStoreInterface.toBytes(base64);
        String toTest = KeyStoreInterface.toBase64String(bytes);
        assert(toTest == base64);
    }

    @Test
    public void TestEncrypt(){
        
    }
}