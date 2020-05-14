package nsu.fit.g14201.marchenko.phoenix.notifications;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.connection.GoogleUserConnection;
import nsu.fit.g14201.marchenko.phoenix.contacts.ContactsDBController;

public class NotificationsPresenter {
    private ContactsDBController dbController;
    private Map<String, String> contacts;
    private final static String SENT = "SENT_SMS_ACTION";
    private Handler handler;

    @SuppressLint("HandlerLeak")
    public NotificationsPresenter(Context context){
        dbController = new ContactsDBController(context);
        contacts = new TreeMap<>(dbController.getContacts(GoogleUserConnection.getInstance().getCredential().getSelectedAccountName()));
        handler = new Handler(){
            public void handleMessage(Message msg){
                Bundle bundle = msg.getData();
                String str = bundle.getString("Key");
                Toast.makeText(context,str,Toast.LENGTH_LONG).show();
            }
        };
    }

    public void call(View v){
        Toast.makeText(v.getContext(), "Emergency call", Toast.LENGTH_LONG).show();
        new Thread(() -> {
            Message msg = handler.obtainMessage();
            Bundle bundle = new Bundle();
            if (contacts.size() != 0) {
                for (Map.Entry e : contacts.entrySet()) {
                    Log.i(App.getTag(), composeMessage(e.getKey().toString()));
                    Log.i(App.getTag(), e.getValue().toString());
                    PendingIntent sentPI = PendingIntent.getBroadcast(v.getContext(), 0, new Intent(SENT), 0);
                    SmsManager smsManager = SmsManager.getDefault();
                    if (composeMessage(e.getKey().toString()).length() > 160) {
                        ArrayList<String> mArray = smsManager.divideMessage(composeMessage(e.getKey().toString()));
                        ArrayList<PendingIntent> sentArrayIntents = new ArrayList<>();
                        for (int i = 0; i < mArray.size(); i++)
                            sentArrayIntents.add(sentPI);
                        smsManager.sendMultipartTextMessage(e.getValue().toString(), null, mArray, sentArrayIntents, null);
                    } else {
                        smsManager.sendTextMessage(e.getValue().toString(), null, composeMessage(e.getKey().toString()), null, null);
                    }
                }
                String msgString = "Sms sent";
                bundle.putString("Key", msgString);
                msg.setData(bundle);
                handler.sendMessage(msg);
            } else {
                String msgString = "No contacts chosen";
                bundle.putString("Key", msgString);
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        }).start();
    }

    private String composeMessage(String name){
        String folderId = App.getAppFolderId();
        String link = "https://drive.google.com/drive/folders/"+folderId+"?usp=sharing";
        return "Здравствуйте " + name + "! Вы получили данное сообщение,так как была нажата тревожная кнопка на устройстве отправляющего абонента. Перейдя по ссылке ниже Вы сможете ознакомиться с видеозаписью происшествия:"+link;
    }
}
