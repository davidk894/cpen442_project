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

import javax.crypto.Cipher;


public class MainActivity extends AppCompatActivity {

    private static final String DIALOG_FRAGMENT_TAG = "myFragment";

    // Permission request codes
    private static final int PERMISSION_READ_PHONE_STATE = 100;

    // Job code
    private static final int JOB_CREATE_ACCOUNT = 0;
    private static final int JOB_INVALID_CODE = 9999;

    // API request URLs
    private static final String CREATE_ACCOUNT_URL =
            "https://zb9evmcr90.execute-api.us-west-2.amazonaws.com/Prod/create-account";
    private static final String AUTHENTICATE_URL =
            "test";

    private SharedPreferences mSharedPreferences;
    private String fpSecret;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerReceiver(new FBReceiver(), new IntentFilter("FBMessage"));
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_READ_PHONE_STATE: {
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
        }
        return;
    }

    public void createAccount() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PermissionChecker.PERMISSION_GRANTED) {
            // Request permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    PERMISSION_READ_PHONE_STATE);
        } else {
            // Already have permissions
            JSONObject reqParams = new JSONObject();
            TelephonyManager mgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String deviceId = mgr.getDeviceId();
            String phoneNumber = mgr.getLine1Number();
            String fcmToken = FirebaseInstanceId.getInstance().getToken();
            try {
                reqParams.put("firebaseID", fcmToken);
                reqParams.put("name", "TestUser");
                reqParams.put("IMEI", deviceId);
                reqParams.put("contactNumber", phoneNumber);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            createAccountRequest(this, reqParams);
        }
    }

    // HTTP Requests
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
                        // Go to authenticator
                        // We won't be making duplicate accounts
                        // If there is a duplicate account, we should've gotten the "fail" response
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
        request.setRequestURL(CREATE_ACCOUNT_URL);
        request.setJSONString(requestParams.toString());
        request.execute();
    }

    /**
     * Start fingerprint authentication
     */
    public void authCreateAccount() {
        if(!KeyStoreInterface.keyExists()) {
            KeyStoreInterface.generateKey();
        }
        Cipher cipher = KeyStoreInterface.cipherInit(Cipher.ENCRYPT_MODE);
        FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);

        FingerprintAuthenticationDialogFragment fragment = new FingerprintAuthenticationDialogFragment();
        fragment.setCryptoObject(cryptoObject);

        fragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
    }

    /**
     * Fingerprint authentication complete, now build request to server
     * @param smsSecret
     * @param withFingerprint
     * @param cryptoObject
     */
    public void onAuthenticated(String smsSecret, boolean withFingerprint,
                                @Nullable FingerprintManager.CryptoObject cryptoObject) {
//        if(withFingerprint) {
//            assert cryptoObject != null;
//            //...
//            JSONObject reqParams = new JSONObject();
//            authenticateRequest(this, reqParams);
//        }
        System.out.println("WOO WE AUTHENTICATED");
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
                        // Verify job-code (that this is createAccount)
                        // Then store encrypted string and userid into shared preferences
                        if(resp.getInt("jobId") == JOB_CREATE_ACCOUNT) {
                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                            editor.putString(getString(R.string.shared_pref_user_id), resp.getString("userId"));
                            editor.putString(getString(R.string.shared_pref_fp_secret), fpSecret);
                            editor.commit();
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
        request.setRequestURL(AUTHENTICATE_URL);
        request.setJSONString(requestParams.toString());
        request.execute();
    }

    // Button click listeners
    public void onCreateBtnClick(View v) {
        createAccount();
    }

    public void testFunction(View v) {
        FingerprintAuthenticationDialogFragment fragment = new FingerprintAuthenticationDialogFragment();
        fragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
    }

    public void startHomeActivity(View v) {
        String userId = mSharedPreferences.getString(getString(R.string.shared_pref_user_id),
                getString(R.string.default_user_id));
        if(!userId.equals(getString(R.string.default_user_id))) {
            Intent enterIntent = new Intent(this, HomeActivity.class);
            startActivity(enterIntent);
        } else {
            Toast.makeText(this, getString(R.string.no_existing_account), Toast.LENGTH_SHORT).show();
        }
    }

    public class FBReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, intent.getStringExtra("message"), Toast.LENGTH_SHORT).show();
        }
    }
}
