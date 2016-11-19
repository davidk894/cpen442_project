package cpen442.securefileshare;

import android.Manifest;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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


public class MainActivity extends AppCompatActivity {

    private static final String DIALOG_FRAGMENT_TAG = "myFragment";

    // Static strings
    private static final String SHARED_PREF_KEY = "cpen442.securefileshare.sharedprefkey";

    // Permission request codes
    private static final int PERMISSION_READ_PHONE_STATE = 100;

    // API request URLs
    private static final String CREATE_ACCOUNT_URL =
            "https://zb9evmcr90.execute-api.us-west-2.amazonaws.com/Prod/create-account";

    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerReceiver(new FBReceiver(), new IntentFilter("FBMessage"));
        mSharedPreferences = getSharedPreferences(SHARED_PREF_KEY, Context.MODE_PRIVATE);
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

    /**
     * POST request to server to create account
     *
     * @param mContext
     * @param requestParams Name, IMEI, PhoneNumber, FirebaseTokenID
     */
    public void createAccountRequest(final Context mContext, JSONObject requestParams) {
        HttpRequestUtility request = new HttpRequestUtility(new HttpRequestUtility.HttpResponseUtility() {
            @Override
            public void processResponse(String response) {
                try {
                    JSONObject resp = new JSONObject(response);
                    boolean success = resp.getBoolean("success");
                    if(success) {
                        // Go to authenticator
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

    // Button click listeners
    public void onCreateBtnClick(View v) {
//        createAccount();
        FingerprintAuthenticationDialogFragment fragment = new FingerprintAuthenticationDialogFragment();
        fragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
    }

    public void startHomeActivity(View v) {
        Intent enterIntent = new Intent(v.getContext(), HomeActivity.class);
        startActivity(enterIntent);
    }

    public class FBReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, intent.getStringExtra("message"), Toast.LENGTH_SHORT).show();
        }
    }
}
