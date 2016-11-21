package cpen442.securefileshare;

import android.app.DialogFragment;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class FingerprintAuthenticationDialogFragment extends DialogFragment
        implements FingerprintUiHelper.Callback {

    private Context mContext;
    private View mView;
    private FingerprintUiHelper mFingerprintUiHelper;
    private FingerprintManager.CryptoObject mCryptoObject;
    private String smsSecret;
    private boolean waitForFingerprint;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        smsSecret = null;
        waitForFingerprint = false;
        // Do not create a new Fragment when the Activity is re-created such as orientation changes.
        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getDialog().setTitle(getString(R.string.authenticate));
        mView = inflater.inflate(R.layout.fingerprint_dialog_container, container, false);
        Button cancelBtn = (Button) mView.findViewById(R.id.cancel_button);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Button nextBtn = (Button) mView.findViewById(R.id.next_button);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText smsSecretField = (EditText) mView.findViewById(R.id.sms_field);
                smsSecret = smsSecretField.getText().toString();
                if(smsSecret != null && !smsSecret.equals("")) {
                    // Hide the sms verification and show fingerprint screen
                    mView.findViewById(R.id.sms_content).setVisibility(View.GONE);
                    mView.findViewById(R.id.fingerprint_content).setVisibility(View.VISIBLE);
                    v.setVisibility(View.GONE);
                    waitForFingerprint = true;
                    mFingerprintUiHelper.startListening(mCryptoObject);
                } else {
                    Toast.makeText(mContext, getString(R.string.invalid_sms), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mFingerprintUiHelper = new FingerprintUiHelper(
                mContext.getSystemService(FingerprintManager.class),
                (ImageView) mView.findViewById(R.id.fingerprint_icon),
                (TextView) mView.findViewById(R.id.fingerprint_status), this);
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(waitForFingerprint) {
            mFingerprintUiHelper.startListening(mCryptoObject);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mFingerprintUiHelper.stopListening();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = context;
    }

    /**
     * Sets the crypto object to be passed in when authenticating with fingerprint.
     */
    public void setCryptoObject(FingerprintManager.CryptoObject cryptoObject) {
        mCryptoObject = cryptoObject;
    }

    @Override
    public void onAuthenticated() {
        dismiss();
        if (mContext instanceof MainActivity) {
            ((MainActivity) mContext).onAuthenticated(smsSecret, true, mCryptoObject);
        } else if (mContext instanceof HomeActivity) {
            ((HomeActivity) mContext).onAuthenticated(smsSecret, true, mCryptoObject);
        }
    }

    @Override
    public void onError() {
        // Uhoh spaghetti-o
    }

    public void readSMSSecret() {
        if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.READ_SMS)
                != PermissionChecker.PERMISSION_GRANTED) {
            // Request permissions
            ActivityCompat.requestPermissions((AppCompatActivity)mContext,
                    new String[]{android.Manifest.permission.READ_SMS},
                    Constants.PERMISSION_READ_SMS);
        } else {
            // Already have permissions now we need to read sms..
        }
    }
}