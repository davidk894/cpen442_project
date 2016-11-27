package cpen442.securefileshare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SMSReceiver extends BroadcastReceiver {
    private static SMSListener mListener;
    public Pattern p = Pattern.compile("^(Secret: ([a-zA-Z0-9]{8}))");
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle data  = intent.getExtras();
        Object[] pdus = (Object[]) data.get("pdus");
        for(int i=0;i<pdus.length;i++)
        {
            // "3gpp" for GSM/UMTS/LTE messages in 3GPP format or
            // "3gpp2" for CDMA/LTE messages in 3GPP2 format.
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i], "3gpp");
            String sender = smsMessage.getDisplayOriginatingAddress();
            String phoneNumber = smsMessage.getDisplayOriginatingAddress();
            String senderNum = phoneNumber;
            String messageBody = smsMessage.getMessageBody();
            if (phoneNumber.equals(Constants.SMS_SECRET_SENDER_NUMBER)) {
                try
                {
                    if(messageBody != null){
                        Matcher m = p.matcher(messageBody);
                        if(m.matches()) {
                            mListener.messageReceived(m.group(2));
                        }
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static void bindListener(SMSListener listener) {
        mListener = listener;
    }

    public interface SMSListener {
        void messageReceived(String message);
    }
}
