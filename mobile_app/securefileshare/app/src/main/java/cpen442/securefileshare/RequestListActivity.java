package cpen442.securefileshare;

import android.*;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.Cipher;

import cpen442.securefileshare.encryption.EncryptedFileFormat;
import cpen442.securefileshare.encryption.EncryptionException;
import cpen442.securefileshare.encryption.FileEncyrption;
import cpen442.securefileshare.encryption.FileFormat;
import cpen442.securefileshare.encryption.FileIO;
import cpen442.securefileshare.encryption.FormatException;
import cpen442.securefileshare.encryption.HashByteWrapper;
import cpen442.securefileshare.encryption.Utility;

public class RequestListActivity extends ListActivity
        implements RequestAndAuthenticationService.IAuthenticatable{

    private SharedPreferences mSharedPreferences;
    private BroadcastReceiver fbReceiver;
    private RequestListAdapter myAdapter;
    private Job jobToRemoveFromList;
    private boolean doJob;
    private ArrayList<Job> jobsList;
    PermissionHandler permissionHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_req_list);

        permissionHandler = new PermissionHandler(this);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        fbReceiver = new FBReceiver();
        registerReceiver(fbReceiver, new IntentFilter(Constants.FB_RECEIVER_INTENT_FILTER));
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String s = getIntent().getStringExtra(Constants.JOBS_LIST_JSON);
        Gson gson = new Gson();
        Job[] jobs = gson.fromJson(s, Job[].class);
        jobsList = new ArrayList<>(Arrays.asList(jobs));

        myAdapter = new RequestListAdapter(this, R.layout.activity_req_list_job_item);
        myAdapter.addAll(jobs);
        setListAdapter(myAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(fbReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(fbReceiver, new IntentFilter("FBMessage"));
    }

    @Override
    public void onBackPressed() {
        Gson gson = new Gson();
        String jsonString = gson.toJson(jobsList,
                new TypeToken<ArrayList<Job>>() {}.getType());
        Intent intent = new Intent();
        intent.putExtra(Constants.JOBS_LIST_JSON, jsonString);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        super.onListItemClick(list, view, position, id);
        Job selectedItem = (Job) getListView().getItemAtPosition(position);
        int jobType = selectedItem.getJobType();
        doJob = true;
        switch(jobType) {
            case Constants.JOB_PENDING_REQUEST: {
                // These are requests the user is waiting a response for
                break;
            }
            case Constants.JOB_PENDING_RESPONSE: {
                showOptionsDialog(selectedItem);
                break;
            }
            case Constants.JOB_GOT_KEY: {
                showDecryptDialog(selectedItem);
                break;
            }
        }
    }

    public void refreshListBtnClick(View v) {
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

    private void showOptionsDialog(final Job item) {
        final String[] listOfItems = new String[] {"Approve Request", "Decline Request"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_option_dialog_title)
               .setItems(listOfItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == Constants.DECLINE_REQUEST) {
                    doJob = false;
                }
                respondKeyRequest(item);
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDecryptDialog(final Job item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.got_key_dialog_title)
               .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       dialog.dismiss();
                   }
               })
               .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       getKeyRequest(item);
                       dialog.dismiss();
                   }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Authorize the key request
    private void respondKeyRequest(Job item) {
        JSONObject requestParams = new JSONObject();
        String userId = mSharedPreferences.getString(Constants.SHARED_PREF_USER_ID, null);
        if(userId != null) {
            try {
                requestParams.put("userID", userId);
                requestParams.put("jobID", item.getJobID());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jobToRemoveFromList = item;

            RequestAndAuthenticationService service =
                    RequestAndAuthenticationService.getInstance();
            service.setCipherMode(Cipher.DECRYPT_MODE);
            service.setSharedPreferences(mSharedPreferences);
            service.setDoJob(doJob);
            service.makeRequest(this, Constants.RESPOND_KEY_URL, requestParams);
        }
    }

    private void getKeyRequest(Job item) {
        JSONObject requestParams = new JSONObject();
        String userId = mSharedPreferences.getString(Constants.SHARED_PREF_USER_ID, null);
        if(userId != null) {
            try {
                requestParams.put("userID", userId);
                requestParams.put("jobID", item.getJobID());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jobToRemoveFromList = item;

            RequestAndAuthenticationService service =
                    RequestAndAuthenticationService.getInstance();
            service.setSharedPreferences(mSharedPreferences);
            service.setCipherMode(Cipher.DECRYPT_MODE);
            service.setDoJob(doJob);
            service.makeRequest(this, Constants.GET_KEY_URL, requestParams);
        }
    }

    @Override
    public void processAuthenticateResponse(JSONObject response) {
        try {
            Integer jobType = response.getInt("jobType");
            switch(jobType) {
                case Constants.JOB_RESPOND_KEY: {
                    if(jobToRemoveFromList != null) {
                        jobsList.remove(jobToRemoveFromList);
                        myAdapter.remove(jobToRemoveFromList);
                        myAdapter.notifyDataSetChanged();
                        jobToRemoveFromList = null;
                        Toast.makeText(this, "Successfully authorized.", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                case Constants.JOB_DELETE_KEY: {
                    if(jobToRemoveFromList != null) {
                        jobsList.remove(jobToRemoveFromList);
                        myAdapter.remove(jobToRemoveFromList);
                        myAdapter.notifyDataSetChanged();
                        jobToRemoveFromList = null;
                        Toast.makeText(this, "Successfully denied request.", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                case Constants.JOB_GOT_KEY: {
                    JSONObject information = response.getJSONObject("information");
                    String key = response.getString("key");
                    String fileHash = response.getString("fileHash");
                    //Decrypt
                    FileAccessPermission fileAccessPermission = new FileAccessPermission();
                    fileAccessPermission.key = Utility.toBytes(key);
                    String fileName = fileHash + Constants.ENCRYPTED_FILE_EXTENTION;
                    fileAccessPermission.filePath = FileIO.combine(Constants.TO_DECRYPT_PATH_FULL,
                            fileName);
                    fileAccessPermission.purpose = FileAccessPermission.Purpose.toDecrypt_read;
                    handleFileAccessPermissionResult(fileAccessPermission);

                    jobToRemoveFromList = null;
                    break;
                }
                case Constants.JOB_GET_JOBS: {
                    JSONArray list = response.getJSONArray("information");
                    Gson gson = new Gson();
                    Job[] jobs = gson.fromJson(list.toString(), Job[].class);
                    jobsList = new ArrayList<>(Arrays.asList(jobs));
                    myAdapter.clear();
                    myAdapter.addAll(jobs);
                    myAdapter.notifyDataSetChanged();
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                        case android.Manifest.permission.READ_PHONE_STATE: {
                            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {

                            } else {

                            }
                            break;
                        }
                        case android.Manifest.permission.READ_SMS: {
                            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {

                            } else {

                            }
                            break;
                        }
                        case android.Manifest.permission.READ_EXTERNAL_STORAGE: {
                            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                                handleFileAccessPermissionResult((FileAccessPermission) permissionHandler.get(requestCode));
                            } else {

                            }
                            break;
                        }
                        case android.Manifest.permission.WRITE_EXTERNAL_STORAGE: {
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
                    if (allGranted){
                        boolean isFileAccess = true;
                        for (String p : permissions){
                            if (p != android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    || p != android.Manifest.permission.READ_EXTERNAL_STORAGE) {
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
        permissionHandler.remove(requestCode);
    }

    public void handleFileAccessPermissionResult(FileAccessPermission fileAccessInfo) {
        switch (fileAccessInfo.purpose) {
            case toDecrypt_read:
                if (!permissionHandler.readFile(fileAccessInfo)) {
                    return;
                }
                try {
                    EncryptedFileFormat eff = new EncryptedFileFormat(fileAccessInfo.fileData);
                    FileFormat ff = FileEncyrption.DecryptFile(eff.getEncryptedData(),
                            fileAccessInfo.key);
                    fileAccessInfo.fileData = ff.GetFileBytes();
                    fileAccessInfo.filePath = ff.getFileName();
                } catch (EncryptionException e) {
                    Toast.makeText(this, "Decryption Failed", Toast.LENGTH_SHORT).show();
                    return;
                } catch (FormatException e) {
                    Toast.makeText(this, "File Format Exception", Toast.LENGTH_SHORT).show();
                    return;
                }
                fileAccessInfo.purpose = FileAccessPermission.Purpose.Decrypt_write;
            case Decrypt_write:
                fileAccessInfo.filePath = FileIO.combine(Constants.DECRYPTED_PATH_FULL,
                        fileAccessInfo.filePath);
                permissionHandler.writeToFile(fileAccessInfo);
                break;
            default:
                Toast.makeText(this, "Shouldn't be here", Toast.LENGTH_SHORT).show();
        }
    }
}
