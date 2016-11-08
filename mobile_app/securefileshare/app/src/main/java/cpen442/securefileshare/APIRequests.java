package cpen442.securefileshare;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class APIRequests {
    /**
     * Creating account
     * @param mContext
     * @param requestParams Name, IMEI, PhoneNumber, FirebaseTokenID
     */
    public static void createAccount(Context mContext, JSONObject requestParams) {
        HttpRequestUtility request = new HttpRequestUtility(mContext);
        request.setRequestType(HttpRequestUtility.POST_METHOD);
        request.setRequestURL("https://zb9evmcr90.execute-api.us-west-2.amazonaws.com/Prod/create-account");
        request.setJSONString(requestParams.toString());
        request.execute();
    }

    // Add key to file
    public static void addKey(Context mContext) {

    }

    // Request key to a file
    public static void requestKey(Context mContext) {
        // empty for now
    }

    // Authorize access to a file
    public static void authorizeAccess(Context mContext) {

    }

    // Authenticate the user
    public static void authenticateUser(Context mContext) {

    }

//
//    private void createAccountRequestExample() {
//        HttpRequestUtility request = new HttpRequestUtility(getApplicationContext());
//        request.setRequestType(HttpRequestUtility.POST_METHOD);
//        request.setRequestURL("https://zb9evmcr90.execute-api.us-west-2.amazonaws.com/Prod/create-account");
//
//        JSONObject requestParams = new JSONObject();
//        try {
//            requestParams.put("name", "AuckFndrew");
//            requestParams.put("IMEI", "AuckFndrew");
//            requestParams.put("contactNumber", "0987654321");
//            requestParams.put("firebaseID", "AuckFndrew");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        request.setJSONString(requestParams.toString());
//        request.execute();
//    }
}
