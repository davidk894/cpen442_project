package cpen442.securefileshare;

import android.content.Context;
import org.json.JSONObject;

public class APIRequest {
    private static final String CREATE_ACCOUNT_URL =
            "https://zb9evmcr90.execute-api.us-west-2.amazonaws.com/Prod/create-account";

    /**
     * Creating account
     * @param mContext
     * @param requestParams Name, IMEI, PhoneNumber, FirebaseTokenID
     */
    public static void createAccount(final Context mContext, JSONObject requestParams) {
        HttpRequestUtility request = new HttpRequestUtility(new HttpRequestUtility.HttpResponseUtility() {
            @Override
            public void processResponse(String response) {
                // do nothing
            }
        });
        request.setRequestType(HttpRequestUtility.POST_METHOD);
        request.setRequestURL(CREATE_ACCOUNT_URL);
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
}
