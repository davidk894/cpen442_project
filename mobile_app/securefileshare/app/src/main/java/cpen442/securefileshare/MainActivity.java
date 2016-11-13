package cpen442.securefileshare;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_READ_PHONE_STATE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button createAccountBtn = (Button) findViewById(R.id.btn_createacc);
        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // View = button
                createAccount();
            }
        });

        registerReceiver(new FBReceiver(), new IntentFilter("FBMessage"));
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

                    // Dont' do anything then..
                }
                break;
            }
        }
        return;
    }

    public void createAccount() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PermissionChecker.PERMISSION_GRANTED) {
            // Request permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    PERMISSION_READ_PHONE_STATE);
        } else {
            // Already have permissions
            JSONObject obj = this.retrieveDeviceInfo();
            String fcmToken = FirebaseInstanceId.getInstance().getToken();
            try {
                obj.put("firebaseID", fcmToken);
                obj.put("name", "TestUser");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            APIRequest.createAccount(this, obj);
        }
    }

    private JSONObject retrieveDeviceInfo() {
        TelephonyManager mgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = mgr.getDeviceId();
        String phoneNumber = mgr.getLine1Number();
        JSONObject obj = new JSONObject();
        try {
            obj.put("IMEI", deviceId);
            obj.put("contactNumber", phoneNumber);
        } catch(JSONException e) {
            // do nothing
        }
        System.out.println("DEVICE ID: " + deviceId + "\n\n");
        System.out.println("PHONE NUMBER: " + phoneNumber + "\n\n");
        return obj;
    }

    public class FBReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, intent.getStringExtra("message"), Toast.LENGTH_SHORT).show();
        }
    }
}
