package cpen442.securefileshare;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Cipher;

public class CreateAccountActivity extends Activity
        implements RequestAndAuthenticationService.IAuthenticatable, SMSReceiver.SMSListener {

    private SharedPreferences mSharedPreferences;
    private String mFpSecret;
    private String mName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SMSReceiver.bindListener(this);
    }

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
                }
                break;
            }
        }
        return;
    }

    // Create account
    public void createAccount() {
        int permission_phone_num = ContextCompat.
                checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE);
        int permission_read_sms = ContextCompat.
                checkSelfPermission(this, Manifest.permission.READ_SMS);
        int permission_recv_sms = ContextCompat.
                checkSelfPermission(this, Manifest.permission.RECEIVE_SMS);

        if(permission_phone_num != PermissionChecker.PERMISSION_GRANTED ||
                permission_read_sms != PermissionChecker.PERMISSION_GRANTED ||
                permission_recv_sms != PermissionChecker.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_PHONE_STATE,
                            android.Manifest.permission.READ_SMS,
                            android.Manifest.permission.RECEIVE_SMS},
                    Constants.PERMISSION_READ_PHONE_STATE);
        } else {
            JSONObject reqParams = new JSONObject();
            TelephonyManager mgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            mFpSecret = KeyStoreInterface.generateCryptoMessage();
            String contactNumber = "+" + mgr.getLine1Number();
            String fcmToken = FirebaseInstanceId.getInstance().getToken();
            try {
                reqParams.put("firebaseID", fcmToken);
                reqParams.put("name", mName);
                reqParams.put("fpSecret", mFpSecret);
                reqParams.put("contactNumber", contactNumber);
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
        service.setFpSecret(mFpSecret);
        service.setSharedPreferences(mSharedPreferences);
        service.makeRequest(this, Constants.CREATE_ACCOUNT_URL, requestParams);
    }

    @Override
    public void messageReceived(String message) {
        RequestAndAuthenticationService service = RequestAndAuthenticationService.getInstance();
        FingerprintAuthenticationDialogFragment fragment = service.getFragment();
        if(fragment != null) {
            fragment.setSMSSecret(message);
            // Set the SMS Secret
        }
    }

    @Override
    public void processAuthenticateResponse(JSONObject response) {
        RequestAndAuthenticationService service = RequestAndAuthenticationService.getInstance();
        mFpSecret = service.getFpSecret(); // This should return encrypted fp secret
        service.setFpSecret(null);
        service.setSharedPreferences(null);
        try {
            String userId = response.getString("information");
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(Constants.SHARED_PREF_USER_ID, userId);
            editor.putString(Constants.SHARED_PREF_FP_SECRET, mFpSecret);
            editor.commit();
            mFpSecret = null;

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Button click listeners
    public void onNextBtnClick(View v) {
        EditText nameField = (EditText) findViewById(R.id.name);
        mName = nameField.getText().toString();
        if(mName.length() > 0) {
            createAccount();
        } else {
            Toast.makeText(this, R.string.prompt_name, Toast.LENGTH_SHORT).show();
        }
    }
}

