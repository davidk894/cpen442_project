package cpen442.securefileshare;

import android.net.Uri;

/**
 * Created by Cyberus on 2016-11-21.
 */


public class FileAccessPermission {
    public String filePath;
    public Uri fileUri;
    public byte[] fileData;
    public byte[] key;
    public String targetID;
    public Purpose purpose;
    public Stage stage;

    public enum Purpose {
        Encrypt,
        Decrypt
    }
    public enum Stage {
        ConvertPath,
        Read,
        Write
    }


    public FileAccessPermission() {}
}
