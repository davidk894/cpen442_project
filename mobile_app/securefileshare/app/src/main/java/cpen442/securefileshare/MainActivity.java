package cpen442.securefileshare;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.createAccountRequestExample();
    }

    private void createAccountRequestExample() {
        HttpRequestUtility request = new HttpRequestUtility(getApplicationContext());
        request.setRequestType(HttpRequestUtility.POST_METHOD);
        request.setRequestURL("https://zb9evmcr90.execute-api.us-west-2.amazonaws.com/Prod/create-account");

        JSONObject requestParams = new JSONObject();
        try {
            requestParams.put("name", "AuckFndrew");
            requestParams.put("IMEI", "AuckFndrew");
            requestParams.put("contactNumber", "0987654321");
            requestParams.put("firebaseID", "AuckFndrew");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        request.setJSONString(requestParams.toString());
        request.execute();
    }
}
