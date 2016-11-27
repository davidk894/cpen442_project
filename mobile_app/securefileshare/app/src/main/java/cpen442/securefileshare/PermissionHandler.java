package cpen442.securefileshare;

import android.*;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import cpen442.securefileshare.encryption.FileIO;

/**
 * Created by Cyberus on 2016-11-26.
 */

public class PermissionHandler {

    private HashMap<Integer,Object> permissionMap = new HashMap<Integer, Object>();
    private int PermissionNumber = 0;
    private Activity activity;

    public PermissionHandler(Activity activity) {
        this.activity = activity;
    }

    public boolean readFile(FileAccessPermission fInfo){
        if (ContextCompat.checkSelfPermission(activity,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionMap.put(PermissionNumber, fInfo);
            activity.requestPermissions(new String[]{
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, PermissionNumber);
            PermissionNumber++;
            return false;
        }
        try {
            fInfo.fileData = FileIO.ReadAllBytes(fInfo.filePath);
            return true;
        } catch (IOException e) {
            Toast.makeText(activity, "Error reading file", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public boolean writeToFile(FileAccessPermission fInfo){
        if (ContextCompat.checkSelfPermission(activity,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionMap.put(PermissionNumber, fInfo);
            activity.requestPermissions(new String[]{
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, PermissionNumber);
            PermissionNumber++;
            return false;
        }
        File dir = new File( new File(fInfo.filePath).getParent() );
        if (!dir.isDirectory()) {
            if(!recursiveMcdir(dir)){
                Toast.makeText(activity, "Could not make dir", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        try {
            FileIO.WriteAllBytes(fInfo.filePath, fInfo.fileData);
        } catch (IOException e) {
            Toast.makeText(activity, "Error writing to file", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean recursiveMcdir(File dir) {
        if (!dir.mkdir()){
            recursiveMcdir(new File(dir.getParent()));
            if(dir.mkdir()){
                return true;
            }
            return false;
        }
        return true;
    }

    //Returns true if you have the permission
    public boolean checkAndRequestPermission(String[] permissions, Object permissionArgs){
        if (ContextCompat.checkSelfPermission(activity,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionMap.put(PermissionNumber, permissionArgs);
            activity.requestPermissions(new String[]{
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, PermissionNumber);
            PermissionNumber++;
            return false;
        }
        return true;
    }

    public void remove(int requestNumber){
        permissionMap.remove(requestNumber);
    }

    public Object get(int requestNumber){
        return permissionMap.get(requestNumber);
    }
}
