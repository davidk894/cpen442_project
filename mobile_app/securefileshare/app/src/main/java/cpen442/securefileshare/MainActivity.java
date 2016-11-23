package cpen442.securefileshare;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;


import java.util.HashMap;

import javax.crypto.Cipher;

import cpen442.securefileshare.encryption.Utility;


public class MainActivity extends AppCompatActivity {

    private SharedPreferences mSharedPreferences;
    private BroadcastReceiver fbReceiver;
    private FingerprintAuthenticationDialogFragment fragment;
    private String fpSecret;
    private String jobId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fbReceiver = new FBReceiver();
        registerReceiver(fbReceiver, new IntentFilter(Constants.FB_RECEIVER_INTENT_FILTER));
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
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
    public void onCreateBtnClick(View v) {
        createAccount();
    }

    public void startHomeActivity(View v) {
        String userId = mSharedPreferences.getString(Constants.SHARED_PREF_USER_ID,
                Constants.INVALID_USER_ID);

        if(!userId.equals(Constants.INVALID_USER_ID)) {
            Intent enterIntent = new Intent(this, HomeActivity.class);
            startActivity(enterIntent);
        } else {
            Toast.makeText(this, getString(R.string.no_existing_account), Toast.LENGTH_SHORT).show();
        }
    }

    public void testFunction(View v) {
//        SharedPreferences.Editor editor = mSharedPreferences.edit();
//        editor.remove(Constants.SHARED_PREF_USER_ID);
//        editor.remove(Constants.SHARED_PREF_FP_SECRET);
//        System.out.println("Removed shared prefs");
//        if(KeyStoreInterface.keyExists()) {
//            KeyStoreInterface.removeKey();
//            System.out.println("Removed key");
//        }
        Intent intent = new Intent(this, ReqListActivity.class);
        String jobsListJson =
                "[" +
                    "{\"userID\": \"123456\", \"fileHash\": \"123456\", \"jobID\":123456\", \"jobType\":\"1\"}," +
                    "{\"userID\": \"123456\", \"fileHash\": \"123456\", \"jobID\":123456\", \"jobType\":\"1\"}," +
                    "{\"userID\": \"123456\", \"fileHash\": \"123456\", \"jobID\":123456\", \"jobType\":\"1\"}," +
                    "{\"userID\": \"123456\", \"fileHash\": \"123456\", \"jobID\":123456\", \"jobType\":\"1\"}," +
                    "{\"userID\": \"123456\", \"fileHash\": \"123456\", \"jobID\":123456\", \"jobType\":\"1\"}," +
                    "{\"userID\": \"123456\", \"fileHash\": \"123456\", \"jobID\":123456\", \"jobType\":\"1\"}," +
                    "{\"userID\": \"123456\", \"fileHash\": \"123456\", \"jobID\":123456\", \"jobType\":\"1\"}," +
                    "{\"userID\": \"111111\", \"fileHash\": \"111111\", \"jobID\":111111\", \"jobType\":\"1\"}" +
                "]";
        intent.putExtra(Constants.JOBS_LIST_JSON, jobsListJson);
        startActivity(intent);
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
                    this.createAccount();
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


    // Create account
    public void createAccount() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PermissionChecker.PERMISSION_GRANTED) {
            // Request permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    Constants.PERMISSION_READ_PHONE_STATE);
        } else {
            // Already have permissions
            JSONObject reqParams = new JSONObject();
            TelephonyManager mgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            fpSecret = KeyStoreInterface.generateCryptoMessage();
            String phoneNumber = "+" + mgr.getLine1Number();
            String fcmToken = FirebaseInstanceId.getInstance().getToken();
            try {
                reqParams.put("firebaseID", fcmToken);
                reqParams.put("name", "TestUser");
                reqParams.put("fpSecret", fpSecret);
                reqParams.put("contactNumber", phoneNumber);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            createAccountRequest(this, reqParams);
        }
    }

    /**
     * POST request to server to create account
     * @param mContext
     * @param requestParams Name, IMEI, PhoneNumber, FirebaseTokenID
     */
    public void createAccountRequest(final Context mContext, JSONObject requestParams) {
        HttpRequestUtility request = new HttpRequestUtility(new HttpRequestUtility.HttpResponseUtility() {
            @Override
            public void processResponse(String response) {
                try {
                    JSONObject resp = new JSONObject(response);
                    if(resp.getBoolean("success")) {
                        jobId = resp.getString("jobID");
                        authCreateAccount();
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
        request.setRequestURL(Constants.CREATE_ACCOUNT_URL);
        request.setJSONString(requestParams.toString());
        request.execute();
    }

    // Authentication
    public void authCreateAccount() {
        if(!KeyStoreInterface.keyExists()) {
            KeyStoreInterface.generateKey();
        }
        Cipher cipher = KeyStoreInterface.cipherInit(Cipher.ENCRYPT_MODE);
        FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);

        fragment = new FingerprintAuthenticationDialogFragment();
        fragment.setCryptoObject(cryptoObject);

        fragment.show(getFragmentManager(), Constants.DIALOG_FRAGMENT_TAG);
    }

    /**
     * Fingerprint authentication complete, now build request to server
     * Normally on authenticate, the encrypted fpSecret would be retrieved
     * from shared preferences and then decrypted with the cryptoObject
     * But since we are only going to be authenticating for create account here
     * the fpSecret should be in memory so we can just use it directly
     * @param smsSecret
     * @param withFingerprint
     * @param cryptoObject
     */
    public void onAuthenticated(String smsSecret, boolean withFingerprint,
                                @Nullable FingerprintManager.CryptoObject cryptoObject) {
        assert(jobId != null);
        assert(cryptoObject != null);

        fragment = null;

        if(withFingerprint) {
            JSONObject reqParams = new JSONObject();
            try {
                reqParams.put("jobID", jobId);
                reqParams.put("fpSecret", fpSecret);
                reqParams.put("smsSecret", smsSecret);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            fpSecret = Utility.toBase64String(KeyStoreInterface.transform(
                    cryptoObject.getCipher(), Utility.toBytes(fpSecret)));
            authenticateRequest(this, reqParams);
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
                    boolean success = resp.getBoolean("success");
                    if(success) {
                        if(resp.getInt("jobType") == Constants.JOB_CREATE_ACCOUNT) {
                            String userId = resp.getString("information");
                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                            editor.putString(Constants.SHARED_PREF_USER_ID, userId);
                            editor.putString(Constants.SHARED_PREF_FP_SECRET, fpSecret);
                            editor.commit();
                            fpSecret = null;
                        }
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
}
