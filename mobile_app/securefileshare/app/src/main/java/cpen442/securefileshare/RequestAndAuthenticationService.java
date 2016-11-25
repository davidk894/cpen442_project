package cpen442.securefileshare;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.Cipher;

public class RequestAndAuthenticationService {
    private String mJobId;
    private int mCipherMode; // Encrypt or Decrypt
    private boolean mDoJob;
    private SharedPreferences mSharedPreferences;
    private String mFpSecret;

    public static RequestAndAuthenticationService instance = new RequestAndAuthenticationService();

    public static RequestAndAuthenticationService getInstance() {
        return instance;
    }

    private RequestAndAuthenticationService() { }

    public int getCipherMode() {
        return mCipherMode;
    }
    public void setCipherMode(int cipherMode) {
        mCipherMode = cipherMode;
    }
    public boolean getDoJob() {
        return mDoJob;
    }
    public void setDoJob(boolean doJob) {
        mDoJob = doJob;
    }
    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }
    public void setSharedPreferences(SharedPreferences sharedPreferences) {
        mSharedPreferences = sharedPreferences;
    }
    public String getFpSecret() {
        return mFpSecret;
    }
    public void setFpSecret(String fpSecret) {
        mFpSecret = fpSecret;
    }

    public void makeRequest(final Context context, String requestURL, JSONObject requestParams) {
        HttpRequestUtility request = new HttpRequestUtility(new HttpRequestUtility.HttpResponseUtility() {
            @Override
            public void processResponse(String response) {
                try {
                    JSONObject resp = new JSONObject(response);
                    if(resp.getBoolean("success")) {
                        mJobId = resp.getString("jobID");
                        authenticate((Activity)context);
                    } else {
                        String respMsg = resp.getString("responseMessage");
                        Toast.makeText(context, respMsg, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        request.setRequestType(HttpRequestUtility.POST_METHOD);
        request.setRequestURL(requestURL);
        request.setJSONString(requestParams.toString());
        request.execute();
    }

    // Authentication
    public void authenticate(Activity context) {
        if(!KeyStoreInterface.keyExists()) {
            KeyStoreInterface.generateKey();
        }

        Cipher cipher = KeyStoreInterface.cipherInit(mCipherMode);
        FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);

        FingerprintAuthenticationDialogFragment fragment = new FingerprintAuthenticationDialogFragment();
        fragment.setCryptoObject(cryptoObject);

        fragment.show(context.getFragmentManager(), Constants.DIALOG_FRAGMENT_TAG);
    }

    public void onAuthenticated(Context context, String smsSecret, boolean withFingerprint,
                                @Nullable FingerprintManager.CryptoObject cryptoObject) {
        assert(mJobId != null);
        assert(cryptoObject != null);

        String encryptedFPSecret = mSharedPreferences.getString(
                Constants.SHARED_PREF_FP_SECRET, null);
        if(encryptedFPSecret != null) {
            mFpSecret = KeyStoreInterface.toBase64String(KeyStoreInterface.transform(
                    cryptoObject.getCipher(), KeyStoreInterface.toBytes(encryptedFPSecret)));
        } else if (mCipherMode == Cipher.ENCRYPT_MODE){
            encryptedFPSecret = KeyStoreInterface.toBase64String(KeyStoreInterface.transform(
                    cryptoObject.getCipher(), KeyStoreInterface.toBytes(mFpSecret)));
        }

        if(mFpSecret != null && withFingerprint) {
            JSONObject reqParams = new JSONObject();
            try {
                reqParams.put("jobID", mJobId);
                reqParams.put("fpSecret", mFpSecret);
                reqParams.put("smsSecret", smsSecret);
                reqParams.put("doJob", mDoJob);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mFpSecret = encryptedFPSecret;
            authenticateRequest(context, reqParams);
        }
    }

    public void authenticateRequest(final Context context, JSONObject requestParams) {
        HttpRequestUtility request = new HttpRequestUtility(new HttpRequestUtility.HttpResponseUtility() {
            @Override
            public void processResponse(String response) {
                try {
                    JSONObject resp = new JSONObject(response);
                    if(resp.getBoolean("success")) {
                        ((IAuthenticatable)context).processAuthenticateResponse(resp);
                    } else {
                        // Toast response message
                        String respMsg = resp.getString("responseMessage");
                        Toast.makeText(context, respMsg, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        request.setRequestType(HttpRequestUtility.POST_METHOD);
        request.setRequestURL(Constants.AUTHENTICATE_URL);
        request.setJSONString(requestParams.toString());
        request.execute();
    }

    public interface IAuthenticatable {
        void processAuthenticateResponse(JSONObject response);
    }
}