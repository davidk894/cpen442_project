package cpen442.securefileshare;

public class Constants {
    public static final String DIALOG_FRAGMENT_TAG = "myFragment";
    public static final String FB_RECEIVER_INTENT_FILTER = "FBMessage";
    public static final String SMS_INBOX = "content://sms/inbox";

    // Permission request codes
    public static final int PERMISSION_READ_PHONE_STATE = 100;
    public static final int PERMISSION_READ_SMS = 200;

    // Job types
    public static final int JOB_INVALID_CODE = -1;
    public static final int JOB_CREATE_ACCOUNT = 1;
    public static final int JOB_GET_JOBS = 2;
    public static final int JOB_RESPOND_KEY = 3;
    public static final int JOB_REQUEST_KEY = 4;

    // Shared preference
    public static final String SHARED_PREF_USER_ID = "shared_pref.user_id";
    public static final String INVALID_USER_ID = "Invalid User ID";
    public static final String SHARED_PREF_FP_SECRET = "shared_pref.fp_secret";
    public static final String INVALID_FP_SECRET = "Invalid FP Secret";

    //URLS
    public static final String ADD_KEY_URL = "https://zb9evmcr90.execute-api.us-west-2.amazonaws.com/Prod/add-key";
    public static final String CREATE_ACCOUNT_URL = "https://zb9evmcr90.execute-api.us-west-2.amazonaws.com/Prod/create-account";
    public static final String AUTHENTICATE_URL = "https://zb9evmcr90.execute-api.us-west-2.amazonaws.com/Prod/do-job";
}
