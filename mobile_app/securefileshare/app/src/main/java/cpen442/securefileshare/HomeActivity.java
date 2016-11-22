package cpen442.securefileshare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ListMenuItemView;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;

import cpen442.securefileshare.encryption.DecryptionQueueFile;
import cpen442.securefileshare.encryption.EncryptedPlusKey;
import cpen442.securefileshare.encryption.EncryptionException;
import cpen442.securefileshare.encryption.FileEncyrption;
import cpen442.securefileshare.encryption.FileIO;
import cpen442.securefileshare.encryption.HashByteWrapper;

public class HomeActivity extends AppCompatActivity {

    private SharedPreferences mSharedPreferences;
    private BroadcastReceiver fbReceiver;
    private FingerprintAuthenticationDialogFragment fragment;
    private String jobId;
    private static final String ENCRYPTED_FILE_EXTENTION = ".crypt";
    private ArrayList<DecryptionQueueFile> DecryptionQueue= new ArrayList<DecryptionQueueFile>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        fbReceiver = new FBReceiver();
        registerReceiver(fbReceiver, new IntentFilter(Constants.FB_RECEIVER_INTENT_FILTER));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(fbReceiver != null) {
            unregisterReceiver(fbReceiver);
        }
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
        if(fbReceiver != null) {
            registerReceiver(fbReceiver, new IntentFilter("FBMessage"));
        }
    }

    // Button click listeners
    public void encryptBtnClick(View v) {
        showFileChooser(Constants.FILE_CHOOSER_ENCRYPT);
    }

    public void decryptBtnClick(View v) {
        decrypt();
    }

    public void reqListBtnClick(View v) {
        Intent intent = new Intent(this, ReqListActivity.class);
        startActivity(intent);
        // do nothing
    }

    // Permissions
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSION_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                } else {
                    // Don't do anything then..
                }
                break;
            }
            case Constants.PERMISSION_READ_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    if(fragment != null) {
                        fragment.readSMSSecret();
                    }
                } else {
                    // Don't do anything then..
                }
                break;
            }
        }
        return;
    }

    //Adds key
    public void addKeyRequest(byte[] fileHash, byte[] key) {
        JSONObject requestParams = new JSONObject();
        String userId = mSharedPreferences.getString(
                Constants.SHARED_PREF_USER_ID, Constants.INVALID_USER_ID);
        if(!userId.equals(Constants.INVALID_USER_ID)) {
            try {
                requestParams.put("userID", userId);
                requestParams.put("fileHash", KeyStoreInterface.toBase64String(fileHash));
                requestParams.put("key", KeyStoreInterface.toBase64String(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            makeRequest(this, Constants.ADD_KEY_URL, requestParams);
        }
    }
    public void requestKey(byte[] fileHash){
        JSONObject requestParams = new JSONObject();
        String userId = mSharedPreferences.getString(
                Constants.SHARED_PREF_USER_ID, Constants.INVALID_USER_ID);
        if(!userId.equals(Constants.INVALID_USER_ID)) {
            try {
                requestParams.put("userID", userId);
                requestParams.put("fileHash", KeyStoreInterface.toBase64String(fileHash));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            makeRequest(this, Constants.REQUEST_KEY_URL, requestParams);
        }
    }

    // Decrypt request
    public void decrypt() {
        JSONObject requestParams = new JSONObject();
        String userId = mSharedPreferences.getString(
                Constants.SHARED_PREF_USER_ID, Constants.INVALID_USER_ID);
        if(!userId.equals(Constants.INVALID_USER_ID)) {
            String targetId = userId;
            String fileHash = "123456";
            try {
                requestParams.put("userID", userId);
                requestParams.put("targetID", targetId);
                requestParams.put("fileHash", fileHash);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        makeRequest(this, Constants.REQUEST_KEY_URL, requestParams);
    }

    // Get jobs
    public void requestList() {
        JSONObject requestParams = new JSONObject();
        makeRequest(this, Constants.GET_JOB_LIST_URL, requestParams);
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
                try {
                    EncryptedPlusKey EPK = FileEncyrption.EncryptFile(filePath);
                    Toast.makeText(this, "Encrypted", Toast.LENGTH_SHORT).show();
                    byte[] fileHash = HashByteWrapper.computeHash(EPK.encryptedFile);
                    addKeyRequest(fileHash, EPK.key);
                    Toast.makeText(this, "Sent Key", Toast.LENGTH_SHORT).show();

                    // write to file
                    FileIO.WriteAllBytes(filePath + ENCRYPTED_FILE_EXTENTION, EPK.encryptedFile);
                } catch (IOException e) {
                    Toast.makeText(this, "IO Exception", Toast.LENGTH_SHORT).show();
                } catch (EncryptionException e) {
                    Toast.makeText(this, "Encryption Failed", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == Constants.FILE_CHOOSER_DECRYPT) {
                try {
                    byte[] fileHash = HashByteWrapper.computeHash(FileIO.ReadAllBytes(filePath));
                    DecryptionQueueFile DQF = new DecryptionQueueFile(filePath, fileHash);
                    requestKey(fileHash);
                    Toast.makeText(this, "Requested Key", Toast.LENGTH_SHORT).show();
                    DecryptionQueue.add(DQF); // move file to "toDecrypt" folder
                } catch (IOException e) {
                    Toast.makeText(this, "IO Exception", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    // Authentication
    public void startAuthenticate() {
        Cipher cipher = KeyStoreInterface.cipherInit(Cipher.DECRYPT_MODE);
        FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);

        fragment = new FingerprintAuthenticationDialogFragment();
        fragment.setCryptoObject(cryptoObject);

        fragment.show(getFragmentManager(), Constants.DIALOG_FRAGMENT_TAG);
    }

    /**
     * Fingerprint authentication complete, now build request to server
     * @param smsSecret
     * @param withFingerprint
     * @param cryptoObject
     */
    public void onAuthenticated(String smsSecret, boolean withFingerprint,
                                @Nullable FingerprintManager.CryptoObject cryptoObject) {
        assert(jobId != null);
        assert(cryptoObject != null);
        fragment = null;

        String encryptedFPSecret = mSharedPreferences.getString(
                Constants.SHARED_PREF_FP_SECRET, Constants.INVALID_FP_SECRET);
        if(!encryptedFPSecret.equals(Constants.INVALID_FP_SECRET)) {
            String fpSecret = KeyStoreInterface.toBase64String(KeyStoreInterface.transform(
                    cryptoObject.getCipher(), KeyStoreInterface.toBytes(encryptedFPSecret)));
            if (withFingerprint) {
                JSONObject reqParams = new JSONObject();
                try {
                    reqParams.put("jobID", jobId);
                    reqParams.put("fpSecret", fpSecret);
                    reqParams.put("smsSecret", smsSecret);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                authenticateRequest(this, reqParams);
            }
        }
    }

    public void authenticateRequest(final Context mContext, JSONObject requestParams) {
        HttpRequestUtility request = new HttpRequestUtility(new HttpRequestUtility.HttpResponseUtility() {
            @Override
            public void processResponse(String response) {
                try {
                    JSONObject resp = new JSONObject(response);
                    if(resp.getBoolean("success")) {
                        processAuthenticateResponse(resp);
                    } else {
                        // Toast response message
                        String respMsg = resp.getString("responseMessage");
                        Toast.makeText(mContext, respMsg, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        request.setRequestType(HttpRequestUtility.POST_METHOD);
        request.setRequestURL(Constants.AUTHENTICATE_URL);
        request.setJSONString(requestParams.toString());
        request.execute();
    }

    private void processAuthenticateResponse(JSONObject resp) {
        try {
            Integer jobType = resp.getInt("jobType");
            switch(jobType) {
                case Constants.JOB_REQUEST_KEY: {
                    break;
                }
                case Constants.JOB_ADD_KEY: {
                    break;
                }
                case Constants.JOB_GET_JOBS: {
                    break;
                }
                default:
                    //do nothing;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Build HTTP request + handle response
    public void makeRequest(final Context mContext, String requestURL, JSONObject requestParams) {
        HttpRequestUtility request = new HttpRequestUtility(new HttpRequestUtility.HttpResponseUtility() {
            @Override
            public void processResponse(String response) {
                try {
                    JSONObject resp = new JSONObject(response);
                    if(resp.getBoolean("success")) {
                        jobId = resp.getString("jobID");
                        startAuthenticate();
                    } else {
                        // Toast response message
                        String respMsg = resp.getString("responseMessage");
                        Toast.makeText(mContext, respMsg, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        request.setRequestType(HttpRequestUtility.POST_METHOD);
        request.setRequestURL(requestURL);
        request.setJSONString(requestParams.toString());
        request.execute();
    }
}
