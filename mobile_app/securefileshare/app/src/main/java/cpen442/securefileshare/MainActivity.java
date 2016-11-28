package cpen442.securefileshare;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ExpandedMenuView;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.crypto.Cipher;

import cpen442.securefileshare.encryption.EncryptedFileFormat;
import cpen442.securefileshare.encryption.EncryptedPlusKey;
import cpen442.securefileshare.encryption.EncryptionException;
import cpen442.securefileshare.encryption.FileEncyrption;
import cpen442.securefileshare.encryption.FileFormat;
import cpen442.securefileshare.encryption.FormatException;
import cpen442.securefileshare.encryption.FileIO;
import cpen442.securefileshare.encryption.HashByteWrapper;
import cpen442.securefileshare.encryption.Utility;


public class MainActivity extends AppCompatActivity
        implements RequestAndAuthenticationService.IAuthenticatable, SMSReceiver.SMSListener {

    private SharedPreferences mSharedPreferences;
    private BroadcastReceiver fbReceiver;

    private JSONArray jobsList;

    PermissionHandler permissionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionHandler = new PermissionHandler(this);

        SMSReceiver.bindListener(this);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        fbReceiver = new FBReceiver();
        registerReceiver(fbReceiver, new IntentFilter(Constants.FB_RECEIVER_INTENT_FILTER));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(fbReceiver != null) {
            unregisterReceiver(fbReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(fbReceiver, new IntentFilter("FBMessage"));
    }

    // Button click listeners
    public void encryptBtnClick(View v) {
        showFileChooser(Constants.FILE_CHOOSER_ENCRYPT);
    }

    public void decryptBtnClick(View v) {
        showFileChooser(Constants.FILE_CHOOSER_DECRYPT);
    }

    public void reqListBtnClick(View v) {
        if(jobsList != null) {
            String jobListString = jobsList.toString();
            Intent intent = new Intent(this, RequestListActivity.class);
            intent.putExtra(Constants.JOBS_LIST_JSON, jobListString);
            startActivityForResult(intent, Constants.ACTIVITY_REQUEST_LIST);
        } else {
            JSONObject requestParams = new JSONObject();
            String userId = mSharedPreferences.getString(Constants.SHARED_PREF_USER_ID, null);
            if(userId != null) {
                try {
                    requestParams.put("userID", userId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                RequestAndAuthenticationService service = RequestAndAuthenticationService.getInstance();
                service.setDoJob(true);
                service.setSharedPreferences(mSharedPreferences);
                service.setCipherMode(Cipher.DECRYPT_MODE);
                service.makeRequest(this, Constants.GET_JOB_LIST_URL, requestParams);
            }
        }
    }

    // Permissions
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if(permissions != null || grantResults != null) {
            if (grantResults.length == permissions.length) {
                int i = 0;
                switch (i = permissions.length) {
                    case 1: {
                        switch (permissions[i]) {
                            case Manifest.permission.READ_PHONE_STATE: {
                                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {

                                } else {

                                }
                                break;
                            }
                            case Manifest.permission.READ_SMS: {
                                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {

                                } else {

                                }
                                break;
                            }
                            case Manifest.permission.READ_EXTERNAL_STORAGE: {
                                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                    handleFileAccessPermissionResult((FileAccessPermission) permissionHandler.get(requestCode));
                                } else {

                                }
                                break;
                            }
                            case Manifest.permission.WRITE_EXTERNAL_STORAGE: {
                                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                    handleFileAccessPermissionResult((FileAccessPermission) permissionHandler.get(requestCode));
                                } else {

                                }
                                break;
                            }
                            default: {

                            }
                        }
                    }
                    case 2: {
                        boolean allGranted = true;
                        for (int g : grantResults) {
                            if (g != PackageManager.PERMISSION_GRANTED) {
                                allGranted = false;
                            }
                        }
                        if (allGranted) {
                            boolean isFileAccess = true;
                            for (String p : permissions) {
                                if (p != Manifest.permission.WRITE_EXTERNAL_STORAGE
                                        || p != Manifest.permission.READ_EXTERNAL_STORAGE) {
                                    isFileAccess = false;
                                }
                            }
                            if (isFileAccess) {
                                handleFileAccessPermissionResult((FileAccessPermission) permissionHandler.get(requestCode));
                            }
                        }

                    }
                    default: {
                        // do nothing
                    }
                }
            }
        }
        permissionHandler.remove(requestCode);
    }

    public void handleFileAccessPermissionResult(FileAccessPermission fileAccessInfo) {
        switch (fileAccessInfo.purpose){
            case Encrypt: {
                switch (fileAccessInfo.stage)
                {
                    case ConvertPath:
                        if (!convertPathHelper(fileAccessInfo)){
                            return;
                        }
                    case Read:
                        if (!permissionHandler.readFile(fileAccessInfo)) {
                            return;
                        }
                        String userId = mSharedPreferences.getString(
                                Constants.SHARED_PREF_USER_ID, null);
                        if (userId == null) {
                            Toast.makeText(this, "Invalid User ID", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        try {
                            EncryptedPlusKey EPK = FileEncyrption.EncryptFile(fileAccessInfo.filePath, fileAccessInfo.fileData, userId);
                            fileAccessInfo.fileData = EPK.encryptedFile.toBytes();

                            Toast.makeText(this, "Encrypted", Toast.LENGTH_SHORT).show();
                            byte[] fileHash = HashByteWrapper.computeHash(fileAccessInfo.fileData);

                            addKeyRequest(fileHash, EPK.key);
                            Toast.makeText(this, "Sent Key", Toast.LENGTH_SHORT).show();

                        } catch (EncryptionException e) {
                            Toast.makeText(this, "Encryption Failed", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        fileAccessInfo.filePath = FileIO.combine(Constants.ENCRYPTED_PATH_FULL,
                                new File(fileAccessInfo.filePath).getName().toString()
                                        + Constants.ENCRYPTED_FILE_EXTENTION);
                        fileAccessInfo.stage = FileAccessPermission.Stage.Write;
                    case Write:
                        if (!permissionHandler.writeToFile(fileAccessInfo)) {
                            return;
                        }
                }
                break;
            }
            case Decrypt: {
                switch (fileAccessInfo.stage) {
                    case ConvertPath:
                        if (!convertPathHelper(fileAccessInfo)) {
                            return;
                        }
                    case Read:
                        if (!permissionHandler.readFile(fileAccessInfo)) {
                            return;
                        }

                        byte[] fileHash = HashByteWrapper.computeHash(fileAccessInfo.fileData);
                        String newFileName = Utility.toBase64String(fileHash)
                                + Constants.ENCRYPTED_FILE_EXTENTION;
                        try {
                            fileAccessInfo.targetID = new EncryptedFileFormat(fileAccessInfo.fileData).getUserId();
                        } catch (FormatException e) {
                            Toast.makeText(this, "File Format Exception", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        fileAccessInfo.filePath = FileIO.combine(Constants.TO_DECRYPT_PATH_FULL, newFileName);
                        fileAccessInfo.stage = FileAccessPermission.Stage.Write;
                    case Write:
                        if (!permissionHandler.writeToFile(fileAccessInfo)) {
                            return;
                        }
                        fileHash = HashByteWrapper.computeHash(fileAccessInfo.fileData);
                        requestKey(fileAccessInfo.targetID, fileHash);
                }
                break;
            }
        }
    }

    private boolean convertPathHelper(FileAccessPermission fInfo){
        boolean havePermission = permissionHandler.checkAndRequestPermission(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE },
                        fInfo);
        if (havePermission) {
            fInfo.filePath = PathConverter.getPath(this, fInfo.fileUri);
            fInfo.stage = FileAccessPermission.Stage.Read;
            return true;
        } else {
            return false;
        }
    }


    //Adds key
    public void addKeyRequest(byte[] fileHash, byte[] key) {
        JSONObject requestParams = new JSONObject();
        String userId = mSharedPreferences.getString(Constants.SHARED_PREF_USER_ID, null);
        if(userId != null) {
            try {
                requestParams.put("userID", userId);
                requestParams.put("fileHash", Utility.toBase64String(fileHash));
                requestParams.put("key", Utility.toBase64String(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RequestAndAuthenticationService service = RequestAndAuthenticationService.getInstance();
            service.setDoJob(true);
            service.setSharedPreferences(mSharedPreferences);
            service.setCipherMode(Cipher.DECRYPT_MODE);
            service.makeRequest(this, Constants.ADD_KEY_URL, requestParams);
        }
    }
    public void requestKey(String targetId, byte[] fileHash){
        JSONObject requestParams = new JSONObject();
        String userId = mSharedPreferences.getString(Constants.SHARED_PREF_USER_ID, null);
        if(userId != null) {
            try {
                requestParams.put("userID", userId);
                requestParams.put("targetID", targetId);
                requestParams.put("fileHash", Utility.toBase64String(fileHash));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RequestAndAuthenticationService service = RequestAndAuthenticationService.getInstance();
            service.setDoJob(true);
            service.setSharedPreferences(mSharedPreferences);
            service.setCipherMode(Cipher.DECRYPT_MODE);
            service.makeRequest(this, Constants.REQUEST_KEY_URL, requestParams);
        }
    }

    // File browser for encrypt/decrypt
    public void showFileChooser(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");      //all files
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), requestCode);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.ACTIVITY_REQUEST_LIST) {
            if(resultCode == RESULT_OK) {
                String jsonArray = data.getStringExtra(Constants.JOBS_LIST_JSON);
                try {
                    jobsList = new JSONArray(jsonArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (resultCode == RESULT_OK) {
                FileAccessPermission fInfo = new FileAccessPermission();
                fInfo.fileUri = data.getData(); // path converter required
                if (requestCode == Constants.FILE_CHOOSER_ENCRYPT) {
                    fInfo.purpose = FileAccessPermission.Purpose.Encrypt;
                } else if (requestCode == Constants.FILE_CHOOSER_DECRYPT) {
                    fInfo.purpose = FileAccessPermission.Purpose.Decrypt;
                }
                fInfo.stage = FileAccessPermission.Stage.ConvertPath;
                handleFileAccessPermissionResult(fInfo);
            }
        }
    }

    @Override
    public void processAuthenticateResponse(JSONObject resp) {
        try {
            Integer jobType = resp.getInt("jobType");
            switch(jobType) {
                case Constants.JOB_REQUEST_KEY: {
                    Toast.makeText(this, "Key Request made, awaiting authoriation.", Toast.LENGTH_SHORT).show();
                    break;
                }
                case Constants.JOB_ADD_KEY: {
                    Toast.makeText(this, "Key added successfully.", Toast.LENGTH_SHORT).show();
                    break;
                }
                case Constants.JOB_GET_JOBS: {
                    jobsList = resp.getJSONArray("information");
                    String jobListString = jobsList.toString();
                    Intent intent = new Intent(this, RequestListActivity.class);
                    intent.putExtra(Constants.JOBS_LIST_JSON, jobListString);
                    startActivityForResult(intent, Constants.ACTIVITY_REQUEST_LIST);
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void messageReceived(String message) {
        RequestAndAuthenticationService service = RequestAndAuthenticationService.getInstance();
        FingerprintAuthenticationDialogFragment fragment = service.getFragment();
        if(fragment != null) {
            fragment.setSMSSecret(message);
            // Set the SMS Secret
        }
    }

}
