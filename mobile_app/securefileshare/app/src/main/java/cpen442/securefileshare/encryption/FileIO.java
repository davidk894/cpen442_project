package cpen442.securefileshare.encryption;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Cyberus on 2016-11-07.
 */

public class FileIO {

    public static byte[] ReadAllBytes(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] fileData = new byte[(int) file.length()];
        DataInputStream dis = new DataInputStream(new FileInputStream(file));
        dis.readFully(fileData);
        dis.close();
        return fileData;
    }
    public static void WriteAllBytes(String filePath, byte[] fileData) throws IOException {
        File file = new File(filePath);
        DataOutputStream dis = new DataOutputStream(new FileOutputStream(file));
        dis.write(fileData);;
        dis.close();
    }

    public static String combine (String path1, String path2)
    {
        File file1 = new File(path1);
        File file2 = new File(file1, path2);
        return file2.getPath();
    }
}

