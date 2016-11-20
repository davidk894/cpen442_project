package cpen442.securefileshare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity {

    private SharedPreferences mSharedPreferences;
    private BroadcastReceiver fbReceiver;
    private FingerprintAuthenticationDialogFragment fragment;
    private String jobId;

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
        // do nothing
    }

    public void decryptBtnClick(View v) {
        // do nothing
    }

    public void reqListBtnClick(View v) {
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

    // Authentication
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

    /**
     * Send authentication request to server and handle response
     * @param mContext
     * @param requestParams
     */
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
                default:
                    //do nothing;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
