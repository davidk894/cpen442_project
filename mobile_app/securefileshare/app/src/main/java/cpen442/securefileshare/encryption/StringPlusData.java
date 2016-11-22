package cpen442.securefileshare.encryption;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Created by Cyberus on 2016-11-22.
 */

public abstract class StringPlusData {
    protected static byte deliminator = 0;
    protected String string;
    protected byte[] data;

    public StringPlusData() {}

    public StringPlusData(byte[] allBytes) throws FormatException {
        int indexOfDeliminator = getIndex(allBytes, deliminator);
        byte[] stringBytes = Arrays.copyOf(allBytes, deliminator);
        string = new String(stringBytes, StandardCharsets.UTF_8);
        data = Arrays.copyOfRange(allBytes, indexOfDeliminator + 1, allBytes.length);
    }

    public StringPlusData(byte[] data, String string) {
        this.string = string;
        this.data = data;
    }

    public byte[] toBytes(){
        byte[] stringBytes = string.getBytes(StandardCharsets.UTF_8);
        byte[] toReturn = new byte[stringBytes.length + data.length + 1];
        System.arraycopy(stringBytes, 0, toReturn, 0, stringBytes.length);
        int indexOfDeliminator = stringBytes.length;
        toReturn[indexOfDeliminator] = deliminator;
        System.arraycopy(data, 0, toReturn, indexOfDeliminator + 1, data.length);
        return toReturn;
    }

    private int getIndex(byte[] arr,byte value) throws FormatException {
        for(int i=0;i<arr.length;i++){

            if(arr[i]==value){
                return i;
            }
        }
        throw new FormatException("Deliminator not found");
    }
}
