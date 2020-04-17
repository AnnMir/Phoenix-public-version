package nsu.fit.g14201.marchenko.phoenix.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.connection.GoogleUserConnection;
import nsu.fit.g14201.marchenko.phoenix.contacts.ContactsDBController;

public class NotificationsPresenter {
    private ContactsDBController dbController;
    private Map<String, String> contacts;
    private final static String SENT = "SENT_SMS_ACTION", DELIVERED = "DELIVERED_SMS_ACTION", ISNULL = "Entered, not all data";

    public NotificationsPresenter(Context context){
        dbController = new ContactsDBController(context);
        contacts = new TreeMap<>(dbController.getContacts(GoogleUserConnection.getInstance().getCredential().getSelectedAccountName()));
    }

    public void call(View v){
        Toast.makeText(v.getContext(), "Emergency call", Toast.LENGTH_LONG).show();
        if(contacts.size() != 0) {
            for (Map.Entry e : contacts.entrySet()) {
                Log.i(App.getTag(), composeMessage(e.getKey().toString()));
                Log.i(App.getTag(), e.getValue().toString());
                PendingIntent sentPI = PendingIntent.getBroadcast(v.getContext(), 0, new Intent(SENT), 0);
                PendingIntent delivertPI = PendingIntent.getBroadcast(v.getContext(), 0, new Intent(DELIVERED), 0);
                SmsManager smsManager = SmsManager.getDefault();
                if (composeMessage(e.getKey().toString()).length() > 160) {
                    ArrayList<String> mArray = smsManager.divideMessage(composeMessage(e.getKey().toString()));
                    ArrayList<PendingIntent> sentArrayIntents = new ArrayList<>();
                    for (int i = 0; i < mArray.size(); i++)
                        sentArrayIntents.add(sentPI);
                    smsManager.sendMultipartTextMessage(e.getValue().toString(), null, mArray, sentArrayIntents, null);
                }else {
                    smsManager.sendTextMessage(e.getValue().toString(), null, composeMessage(e.getKey().toString()), null, null);
                }
            }
        }else{
            Toast.makeText(v.getContext(), "No contacts chosen", Toast.LENGTH_LONG).show();
        }
    }

    private String composeMessage(String name){
        return "Здравствуйте " + name + "! Вы получили данное сообщение,так как была нажата тревожная кнопка на устройстве отправляющего абонента. Перейдя по ссылке ниже Вы сможете ознакомиться с видеозаписью происшествия:";
    }
}
