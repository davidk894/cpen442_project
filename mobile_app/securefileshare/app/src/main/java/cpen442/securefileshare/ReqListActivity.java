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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;

public class ReqListActivity extends ListActivity {

    private SharedPreferences mSharedPreferences;
    private BroadcastReceiver fbReceiver;
    private FingerprintAuthenticationDialogFragment fragment;
    private TextView text;
    private String jobId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_req_list);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        fbReceiver = new FBReceiver();
        registerReceiver(fbReceiver, new IntentFilter(Constants.FB_RECEIVER_INTENT_FILTER));
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        text = (TextView) findViewById(R.id.mainText);

        String s = getIntent().getStringExtra(Constants.JOBS_LIST_JSON);
        Gson gson = new Gson();
        Job[] jobs = gson.fromJson(s, Job[].class);

        RequestListAdapter myAdapter = new RequestListAdapter(this, R.layout.activity_req_list_job_item);
        myAdapter.addAll(jobs);
        setListAdapter(myAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(fbReceiver != null) {
            unregisterReceiver(fbReceiver);
            fbReceiver = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(fbReceiver != null) {
            unregisterReceiver(fbReceiver);
            fbReceiver = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fbReceiver = new FBReceiver();
        registerReceiver(fbReceiver, new IntentFilter("FBMessage"));
    }

    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        super.onListItemClick(list, view, position, id);
        Job selectedItem = (Job) getListView().getItemAtPosition(position);
        int jobType = selectedItem.getJobType();
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
                // This is where we show them the option to decrypt or not
                // showOptionsDialog?
                break;
            }
        }
        text.setText("You clicked " + selectedItem.getUserID() + " at position " + position);
    }

    private void showOptionsDialog(Job item) {
        ArrayList<String> array = new ArrayList<>();
        array.add(Constants.APPROVE_REQUEST);
        array.add(Constants.DECLINE_REQUEST);
        final String[] listOfItems = new String[array.size()];
        array.toArray(listOfItems);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select an option")
               .setItems(listOfItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedItem = listOfItems[which];
                if(selectedItem.equals(Constants.APPROVE_REQUEST)) {
                    // they approved
                } else if(selectedItem.equals(Constants.DECLINE_REQUEST)) {
                    // they denied
                }
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDecryptDialog(Job item) {
        // Show a dialog asking if they want to decrypt the file or not
    }

    // Authorize the key request
    private void authorizeKeyRequest() {

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
                    reqParams.put("doJob", true); // this is false when we want to delete the job
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
                    break;
                }
                case Constants.JOB_DELETE_KEY: {
                    break;
                }
                case Constants.JOB_GOT_KEY: {
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
