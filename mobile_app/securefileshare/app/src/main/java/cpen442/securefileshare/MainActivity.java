package cpen442.securefileshare;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_READ_PHONE_STATE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.getDeviceInformation();
//        JSONObject reqParams = new JSONObject();
//        try {
//            reqParams.put("name", "AuckFndrew");
//            reqParams.put("IMEI", "AuckFndrew");
//            reqParams.put("contactNumber", "0987654321");
//            reqParams.put("firebaseID", "AuckFndrew");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        APIRequests.createAccount(this, reqParams);
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
                    this.getDeviceInformation();

                } else {

                    // Dont' do anything then..
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    /**
     * This method checks for permission before actually retrieving device information
     */
    public void getDeviceInformation() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PermissionChecker.PERMISSION_GRANTED) {
            // Request permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    PERMISSION_READ_PHONE_STATE);
        } else {
            // Already have permissions
            this.getDeviceInformation();
        }
    }

    private void retrieveDeviceInfo() {
        TelephonyManager mgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = mgr.getDeviceId();
        String phoneNumber = mgr.getLine1Number();
        System.out.println("DEVICE ID: " + deviceId + "\n\n");
        System.out.println("PHONE NUMBER: " + phoneNumber + "\n\n");
    }
}
