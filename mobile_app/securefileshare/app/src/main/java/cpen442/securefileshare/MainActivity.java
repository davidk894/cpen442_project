package cpen442.securefileshare;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import javax.crypto.Cipher;

import cpen442.securefileshare.encryption.EncryptionException;
import cpen442.securefileshare.encryption.FileEncyrption;
import cpen442.securefileshare.encryption.FileFormat;
import cpen442.securefileshare.encryption.FormatException;
import cpen442.securefileshare.encryption.FileIO;
import cpen442.securefileshare.encryption.HashByteWrapper;

public class MainActivity extends AppCompatActivity
        implements RequestAndAuthenticationService.IAuthenticatable {

    private SharedPreferences mSharedPreferences;
    private BroadcastReceiver fbReceiver;
    private static final String ENCRYPTED_FILE_EXTENTION = ".crypt";
    private HashMap<Integer,Object> permissionMap = new HashMap<Integer, Object>();
    private int PermissionNumber = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
//        JSONObject requestParams = new JSONObject();
//        String userId = mSharedPreferences.getString(Constants.SHARED_PREF_USER_ID, null);
//        if(userId != null) {
//            try {
//                requestParams.put("userID", userId);
//                requestParams.put("fileHash", "1234567890");
//                requestParams.put("key", "1234567890");
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            RequestAndAuthenticationService service = RequestAndAuthenticationService.getInstance();
//            service.setDoJob(true);
//            service.setSharedPreferences(mSharedPreferences);
//            service.setCipherMode(Cipher.DECRYPT_MODE);
//            service.makeRequest(this, Constants.ADD_KEY_URL, requestParams);
//        }
    }

    public void decryptBtnClick(View v) {
//        JSONObject requestParams = new JSONObject();
//        String userId = mSharedPreferences.getString(Constants.SHARED_PREF_USER_ID, null);
//        if(userId != null) {
//            try {
//                requestParams.put("targetID", userId);
//                requestParams.put("fileHash", "1234567890");
//                requestParams.put("userID", userId);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            RequestAndAuthenticationService service = RequestAndAuthenticationService.getInstance();
//            service.setDoJob(true);
//            service.setSharedPreferences(mSharedPreferences);
//            service.setCipherMode(Cipher.DECRYPT_MODE);
//            service.makeRequest(this, Constants.REQUEST_KEY_URL, requestParams);
//        }
    }

    public void reqListBtnClick(View v) {
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

    public void testFunction(View v) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(Constants.SHARED_PREF_USER_ID);
        editor.remove(Constants.SHARED_PREF_FP_SECRET);
        editor.commit();
        System.out.println("Removed shared prefs");
        if(KeyStoreInterface.keyExists()) {
            KeyStoreInterface.removeKey();
            System.out.println("Removed key");
        }
//        Intent intent = new Intent(this, RequestListActivity.class);
//        String jobsListJson =
//                "[" +
//                    "{\"userID\": \"123456\", \"fileHash\": \"123456\", \"jobID\":123456\", \"jobType\":\"8\", \"contactNumber\":\"1234567890\", \"name\":\"testuser\"}," +
//                    "{\"userID\": \"123456\", \"fileHash\": \"123456\", \"jobID\":123456\", \"jobType\":\"8\", \"contactNumber\":\"1234567890\", \"name\":\"testuser\"}," +
//                    "{\"userID\": \"123456\", \"fileHash\": \"123456\", \"jobID\":123456\", \"jobType\":\"8\", \"contactNumber\":\"1234567890\", \"name\":\"testuser\"}," +
//                    "{\"userID\": \"123456\", \"fileHash\": \"123456\", \"jobID\":123456\", \"jobType\":\"8\", \"contactNumber\":\"1234567890\", \"name\":\"testuser\"}," +
//                    "{\"userID\": \"123456\", \"fileHash\": \"123456\", \"jobID\":123456\", \"jobType\":\"8\", \"contactNumber\":\"1234567890\", \"name\":\"testuser\"}," +
//                    "{\"userID\": \"123456\", \"fileHash\": \"123456\", \"jobID\":123456\", \"jobType\":\"8\", \"contactNumber\":\"1234567890\", \"name\":\"testuser\"}," +
//                    "{\"userID\": \"123456\", \"fileHash\": \"123456\", \"jobID\":123456\", \"jobType\":\"8\", \"contactNumber\":\"1234567890\", \"name\":\"testuser\"}," +
//                    "{\"userID\": \"111111\", \"fileHash\": \"111111\", \"jobID\":111111\", \"jobType\":\"9\", \"contactNumber\":\"1234567890\"}" +
//                "]";
//        intent.putExtra(Constants.JOBS_LIST_JSON, jobsListJson);
//        startActivity(intent);
    }

    // Permissions
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if(grantResults.length == permissions.length) {
            int i;
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
                                handleFileAccessPermissionResult((FileAccessPermision) permissionMap.get(requestCode));
                            } else {

                            }
                            break;
                        }
                        case Manifest.permission.WRITE_EXTERNAL_STORAGE: {
                            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                handleFileAccessPermissionResult((FileAccessPermision) permissionMap.get(requestCode));
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
                    if (allGranted){
                        boolean isFileAccess = true;
                        for (String p : permissions){
                            if (p != Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    || p != Manifest.permission.READ_EXTERNAL_STORAGE) {
                                isFileAccess = false;
                            }
                        }
                        if (isFileAccess) {
                            handleFileAccessPermissionResult((FileAccessPermision) permissionMap.get(requestCode));
                        }
                    }

                }
                default: {
                    // do nothing
                }
            }
        }
        permissionMap.remove(requestCode);
    }

    private void handleFileAccessPermissionResult(FileAccessPermision fileAccessInfo) {
        switch (fileAccessInfo.purpose) {
            case Encrypt_read:
                if (!readFile(fileAccessInfo)) {
                    return;
                }
//                try {
//                    EncryptedPlusKey EPK = FileEncyrption.EncryptFile(fileAccessInfo.filePath, fileAccessInfo.fileData);
//                    fileAccessInfo.fileData = EPK.encryptedFile;
//
//                    Toast.makeText(this, "Encrypted", Toast.LENGTH_SHORT).show();
//                    byte[] fileHash = HashByteWrapper.computeHash(EPK.encryptedFile);
//
//                    addKeyRequest(fileHash, EPK.key);
//                    Toast.makeText(this, "Sent Key", Toast.LENGTH_SHORT).show();
//
//                } catch (EncryptionException e) {
//                    Toast.makeText(this, "Encryption Failed", Toast.LENGTH_SHORT).show();
//                    return;
//                }
                //Fallthrough
                fileAccessInfo.purpose = FileAccessPermision.Purpose.Encrypt_write;
            case Encrypt_write:
                if (!writeToFile(fileAccessInfo)) {
                    return;
                }
                break;
            case Decrypt_read:
                if (!readFile(fileAccessInfo)) {
                    return;
                }
                fileAccessInfo.purpose = FileAccessPermision.Purpose.toDecrypt_write;
            case toDecrypt_write:
                if (!writeToFile(fileAccessInfo)) {
                    return;
                }
                byte[] fileHash = HashByteWrapper.computeHash(fileAccessInfo.fileData);
                requestKey(fileAccessInfo.targetID, fileHash);
                break;
            case toDecrypt_read:
                if (!readFile(fileAccessInfo)) {
                    return;
                }
                try {
                    FileFormat ff = FileEncyrption.DecryptFile(fileAccessInfo.filePath,
                            fileAccessInfo.key);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (EncryptionException e) {
                    Toast.makeText(this, "Decryption Failed", Toast.LENGTH_SHORT).show();
                    return;
                } catch (FormatException e) {
                    Toast.makeText(this, "File Format Exception", Toast.LENGTH_SHORT).show();
                    return;
                }
                fileAccessInfo.purpose = FileAccessPermision.Purpose.Decrypt_write;
            case Decrypt_write:
                writeToFile(fileAccessInfo);
                break;
            default:
                Toast.makeText(this, "Shouldn't be here", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean readFile(FileAccessPermision fInfo){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionMap.put(PermissionNumber, fInfo);
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, PermissionNumber);
            PermissionNumber++;
            return false;
        }
        try {
            fInfo.fileData = FileIO.ReadAllBytes(fInfo.filePath);
            return true;
        } catch (IOException e) {
            Toast.makeText(this, "Error reading file", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean writeToFile(FileAccessPermision fInfo){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionMap.put(PermissionNumber, fInfo);
            requestPermissions(new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, PermissionNumber);
            PermissionNumber++;
            return false;
        }
        try {
            FileIO.WriteAllBytes(fInfo.filePath, fInfo.fileData);
        } catch (IOException e) {
            Toast.makeText(this, "Error writing to file", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //Adds key
    public void addKeyRequest(byte[] fileHash, byte[] key) {
        JSONObject requestParams = new JSONObject();
        String userId = mSharedPreferences.getString(Constants.SHARED_PREF_USER_ID, null);
        if(userId != null) {
            try {
                requestParams.put("userID", userId);
                requestParams.put("fileHash", KeyStoreInterface.toBase64String(fileHash));
                requestParams.put("key", KeyStoreInterface.toBase64String(key));
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
                requestParams.put("fileHash", KeyStoreInterface.toBase64String(fileHash));
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
        if (resultCode == RESULT_OK) {
            Uri fileUri = data.getData();
            String filePath = fileUri.toString();
            if (requestCode == Constants.FILE_CHOOSER_ENCRYPT) {
                //request p
                FileAccessPermision fInfo = new FileAccessPermision();
                fInfo.purpose = FileAccessPermision.Purpose.Encrypt_read;
                fInfo.filePath = filePath;
                handleFileAccessPermissionResult(fInfo);
            } else if (requestCode == Constants.FILE_CHOOSER_DECRYPT) {
                FileAccessPermision fInfo = new FileAccessPermision();
                fInfo.purpose = FileAccessPermision.Purpose.Decrypt_read;
                fInfo.filePath = filePath;
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
                    String responseMessage = resp.getString("responseMessage");
                    Toast.makeText(this, responseMessage, Toast.LENGTH_SHORT).show();
                    break;
                }
                case Constants.JOB_ADD_KEY: {
                    String responseMessage = resp.getString("responseMessage");
                    Toast.makeText(this, responseMessage, Toast.LENGTH_SHORT).show();
                    break;
                }
                case Constants.JOB_GET_JOBS: {
                    JSONArray jobList = resp.getJSONArray("information");
                    String jobListString = jobList.toString();
                    Intent intent = new Intent(this, RequestListActivity.class);
                    intent.putExtra(Constants.JOBS_LIST_JSON, jobListString);
                    startActivity(intent);
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
