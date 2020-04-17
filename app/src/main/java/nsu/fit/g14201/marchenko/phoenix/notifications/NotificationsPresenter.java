package nsu.fit.g14201.marchenko.phoenix.notifications;

import android.content.Context;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import nsu.fit.g14201.marchenko.phoenix.connection.GoogleUserConnection;
import nsu.fit.g14201.marchenko.phoenix.contacts.ContactsDBController;

public class NotificationsPresenter {
    private ContactsDBController dbController;
    private Map<String, String> contacts = new HashMap<>();
    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> numbers = new ArrayList<>();

    public NotificationsPresenter(Context context){
        dbController = new ContactsDBController(context);
        contacts = new TreeMap<>(dbController.getContacts(GoogleUserConnection.getInstance().getCredential().getSelectedAccountName()));
        if(contacts.size()!=0){
            for (Map.Entry e: contacts.entrySet()) {
                names.add(e.getKey().toString());
                numbers.add(e.getValue().toString());
            }
        }
    }

    public void call(View v){
        Toast.makeText(v.getContext(), "Emergency call", Toast.LENGTH_LONG).show();
        for(Map.Entry e: contacts.entrySet()){
            SmsManager.getDefault().sendTextMessage(e.getValue().toString(),null, composeMessage(e.getKey().toString()), null, null);
        }
    }

    private String composeMessage(String name){
        return "Привет "+ name + "!";
    }
}
