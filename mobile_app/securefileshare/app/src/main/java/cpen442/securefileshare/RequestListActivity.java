package cpen442.securefileshare;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import javax.crypto.Cipher;

public class RequestListActivity extends ListActivity {

    private SharedPreferences mSharedPreferences;
    private BroadcastReceiver fbReceiver;
    private FingerprintAuthenticationDialogFragment fragment;
    private RequestListAdapter myAdapter;
    private Job jobToRemoveFromList;
    private String jobId;
    private boolean doJob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_req_list);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        fbReceiver = new FBReceiver();
        registerReceiver(fbReceiver, new IntentFilter(Constants.FB_RECEIVER_INTENT_FILTER));
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String s = getIntent().getStringExtra(Constants.JOBS_LIST_JSON);
        Gson gson = new Gson();
        Job[] jobs = gson.fromJson(s, Job[].class);

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
            makeRequest(this, Constants.RESPOND_KEY_URL, requestParams);
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
            makeRequest(this, Constants.GET_KEY_URL, requestParams);
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
                        // job id
                        jobId = resp.getString("jobID");
                        // start the authentication process
                        startAuthenticate();
                    } else {
                        // Toast response message
                        jobToRemoveFromList = null;
                        String respMsg = resp.getString("responseMessage");
                        Toast.makeText(mContext, respMsg, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    jobToRemoveFromList = null;
                    e.printStackTrace();
                }
            }
        });
        request.setRequestType(HttpRequestUtility.POST_METHOD);
        request.setRequestURL(requestURL);
        request.setJSONString(requestParams.toString());
        request.execute();
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
                Constants.SHARED_PREF_FP_SECRET, null);
        if(encryptedFPSecret != null) {
            String fpSecret = KeyStoreInterface.toBase64String(KeyStoreInterface.transform(
                    cryptoObject.getCipher(), KeyStoreInterface.toBytes(encryptedFPSecret)));
            if (withFingerprint) {
                JSONObject reqParams = new JSONObject();
                try {
                    reqParams.put("jobID", jobId);
                    reqParams.put("fpSecret", fpSecret);
                    reqParams.put("smsSecret", smsSecret);
                    reqParams.put("doJob", doJob); // this is false when we want to delete the job
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
                case Constants.JOB_RESPOND_KEY: {
                    if(jobToRemoveFromList != null) {
                        myAdapter.remove(jobToRemoveFromList);
                        myAdapter.notifyDataSetChanged();
                        jobToRemoveFromList = null;
                    }
                    break;
                }
                case Constants.JOB_DELETE_KEY: {
                    if(jobToRemoveFromList != null) {
                        myAdapter.remove(jobToRemoveFromList);
                        myAdapter.notifyDataSetChanged();
                        jobToRemoveFromList = null;
                    }
                    break;
                }
                case Constants.JOB_GOT_KEY: {
                    JSONObject information = resp.getJSONObject("information");
                    String key = resp.getString("key");
                    String fileHash = resp.getString("fileHash");
                    // do decrypt
                    jobToRemoveFromList = null;
                    break;
                }
                default:
                    //do nothing;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
