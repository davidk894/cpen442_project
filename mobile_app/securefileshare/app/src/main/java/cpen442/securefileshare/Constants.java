package cpen442.securefileshare;

public class Constants {
    public static final String DIALOG_FRAGMENT_TAG = "myFragment";
    public static final String FB_RECEIVER_INTENT_FILTER = "FBMessage";
    public static final String JOBS_LIST_JSON = "JOBS_LIST_JSON";
    public static final String SMS_SECRET_SENDER_NUMBER = "+16505556789";

    // Request list actions
    public static final int APPROVE_REQUEST = 0;
    public static final int DECLINE_REQUEST = 1;

    public static final int FILE_CHOOSER_ENCRYPT = 300;
    public static final int FILE_CHOOSER_DECRYPT = 301;
    public static final int ACTIVITY_REQUEST_LIST = 400;

    // Permission request codes
    public static final int PERMISSION_READ_PHONE_STATE = 100;
    public static final int PERMISSION_READ_SMS = 200;

    // Job types
    public static final int JOB_CREATE_ACCOUNT = 1;
    public static final int JOB_GET_JOBS = 2;
    public static final int JOB_RESPOND_KEY = 3;
    public static final int JOB_REQUEST_KEY = 4;
    public static final int JOB_ADD_KEY = 5;
    public static final int JOB_DELETE_KEY = 6;
    public static final int JOB_GET_KEYS = 7;
    public static final int JOB_PENDING_RESPONSE = 8;
    public static final int JOB_PENDING_REQUEST = 9;
    public static final int JOB_GOT_KEY = 10;

    // Shared preference
    public static final String SHARED_PREF_USER_ID = "shared_pref.user_id";
    public static final String SHARED_PREF_FP_SECRET = "shared_pref.fp_secret";

    //URLS
    public static final String ADD_KEY_URL = "https://zb9evmcr90.execute-api.us-west-2.amazonaws.com/Prod/add-key";
    public static final String REQUEST_KEY_URL = "https://zb9evmcr90.execute-api.us-west-2.amazonaws.com/Prod/request-key";
    public static final String RESPOND_KEY_URL = "https://zb9evmcr90.execute-api.us-west-2.amazonaws.com/Prod/respond-key";
    public static final String GET_KEY_URL = "https://zb9evmcr90.execute-api.us-west-2.amazonaws.com/Prod/get-key";
    public static final String GET_JOB_LIST_URL ="https://zb9evmcr90.execute-api.us-west-2.amazonaws.com/Prod/get-jobs";
    public static final String CREATE_ACCOUNT_URL = "https://zb9evmcr90.execute-api.us-west-2.amazonaws.com/Prod/create-account";
    public static final String AUTHENTICATE_URL = "https://zb9evmcr90.execute-api.us-west-2.amazonaws.com/Prod/do-job";
}
