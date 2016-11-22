package cpen442.securefileshare.encryption;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Created by Cyberus on 2016-11-06.
 */

public class FileFormat {
    protected static byte fileNameDeliminator = 0;
    protected String fileName;
    protected byte[] fileBytes;

    public FileFormat(){}

    public FileFormat(byte[] allBytes) throws FileFormatException {
        int indexOfDeliminator = getIndex(allBytes, fileNameDeliminator);
        byte[] fileNameBytes = Arrays.copyOf(allBytes, fileNameDeliminator);
        fileName = new String(fileNameBytes, StandardCharsets.UTF_8);
        fileBytes = Arrays.copyOfRange(allBytes, indexOfDeliminator + 1, allBytes.length);
    }

    public FileFormat(byte[] fileBytes, String fileName)
    {
        this.fileBytes = fileBytes;
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] GetFileBytes(){
        return fileBytes;
    }

    public byte[] toBytes(){
        byte[] fileNameBytes = toString().getBytes(StandardCharsets.UTF_8);
        byte[] toReturn = new byte[fileBytes.length + fileBytes.length + 1];
        System.arraycopy(fileNameBytes, 0, toReturn, 0, fileNameBytes.length);
        int indexOfDeliminator = fileNameBytes.length;
        toReturn[indexOfDeliminator] = fileNameDeliminator;
        System.arraycopy(fileBytes, 0, toReturn, indexOfDeliminator + 1, fileBytes.length);
        return toReturn;
    }

    private int getIndex(byte[] arr,byte value) throws FileFormatException {
        for(int i=0;i<arr.length;i++){

            if(arr[i]==value){
                return i;
            }
        }
        throw new FileFormatException("Deliminator not found");
    }


}


