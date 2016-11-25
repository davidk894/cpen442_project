package cpen442.securefileshare;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
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


public class MainActivity extends AppCompatActivity
        implements RequestAndAuthenticationService.IAuthenticatable {

    private SharedPreferences mSharedPreferences;
    private BroadcastReceiver fbReceiver;
    private String fpSecret;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fbReceiver = new FBReceiver();
        registerReceiver(fbReceiver, new IntentFilter(Constants.FB_RECEIVER_INTENT_FILTER));
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
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

    // Button click listeners
    public void onCreateBtnClick(View v) {
        createAccount();
    }

    public void startHomeActivity(View v) {
        String userId = mSharedPreferences.getString(Constants.SHARED_PREF_USER_ID, null);

        if(userId != null) {
            Intent enterIntent = new Intent(this, HomeActivity.class);
            startActivity(enterIntent);
        } else {
            Toast.makeText(this, getString(R.string.no_existing_account), Toast.LENGTH_SHORT).show();
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
            createAccountRequest(reqParams);
        }
    }

    public void createAccountRequest(JSONObject requestParams) {
        RequestAndAuthenticationService service = RequestAndAuthenticationService.getInstance();

        service.setCipherMode(Cipher.ENCRYPT_MODE);
        service.setDoJob(true);
        service.setFpSecret(fpSecret);
        service.setSharedPreferences(mSharedPreferences);
        service.makeRequest(this, Constants.CREATE_ACCOUNT_URL, requestParams);
    }

    @Override
    public void processAuthenticateResponse(JSONObject response) {
        RequestAndAuthenticationService service = RequestAndAuthenticationService.getInstance();
        fpSecret = service.getFpSecret(); // This should return encrypted fp secret
        service.setFpSecret(null);
        service.setSharedPreferences(null);
        try {
            String userId = response.getString("information");
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(Constants.SHARED_PREF_USER_ID, userId);
            editor.putString(Constants.SHARED_PREF_FP_SECRET, fpSecret);
            editor.commit();
            fpSecret = null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
