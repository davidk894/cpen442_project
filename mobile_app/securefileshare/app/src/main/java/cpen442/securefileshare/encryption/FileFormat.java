package cpen442.securefileshare.encryption;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Created by Cyberus on 2016-11-06.
 */

public class FileFormat extends StringPlusData {
    public FileFormat(){}

    public FileFormat(byte[] allBytes) throws FormatException  { super(allBytes); }

    public FileFormat(byte[] fileBytes, String fileName) { super(fileBytes, fileName); }

    public String getFileName() { return string; }

    public byte[] GetFileBytes(){
        return data;
    }
}


