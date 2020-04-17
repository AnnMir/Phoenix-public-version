package nsu.fit.g14201.marchenko.phoenix.notifications;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

import nsu.fit.g14201.marchenko.phoenix.App;

public class SendSmsCheck extends BroadcastReceiver {

    private final static String MY_TAG = App.getTag();

    public void onReceive(Context context, Intent intent) {
        switch(getResultCode()) {
            case Activity.RESULT_OK:
                Log.i(MY_TAG, "SMS send");
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                Log.i(MY_TAG, "unknown problems");
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                Log.i(MY_TAG, "modul is down");
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                Log.i(MY_TAG, "PDU error");
                break;
        }
    }

}
