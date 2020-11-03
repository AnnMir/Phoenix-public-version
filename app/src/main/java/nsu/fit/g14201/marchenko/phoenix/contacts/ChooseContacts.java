package nsu.fit.g14201.marchenko.phoenix.contacts;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.contacts.adapter.ContactsAdapter;

public class ChooseContacts extends AppCompatActivity implements ContactsInterface{
    private Map<String, String> contacts;
    private ArrayList<String> names;
    private ArrayList<String> numbers;
    private RecyclerView recyclerView;
    private ContactsAdapter adapter;

    public void choose(String name, String number){
        Intent intent = new Intent(this.getApplicationContext(), ContactsActivity.class);
        intent.putExtra("Class", "ChooseContacts");
        intent.putExtra("Name", name);
        intent.putExtra("Number", number);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_contacts_activity);
        //получить номера
        initRecyclerView();
        loadContacts();
    }

    private void initRecyclerView(){
        recyclerView = findViewById(R.id.recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(mDividerItemDecoration);
        adapter = new ContactsAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this.getApplicationContext(), recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
            @Override public void onItemClick(View view, int position) {
                choose(adapter.getName(position), adapter.getNumber(position));
            }

            @Override public void onLongItemClick(View view, int position) {}
        }));
    }

    public void loadContacts() {
        //получить номера
        getContacts();
        adapter.setItems(contacts);
    }

    public void getContacts(){
        String phoneNumber = null;

        //Связываемся с контактными данными и берем с них значения id контакта, имени контакта и его номера:
        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;


        contacts = new HashMap<>();
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(CONTENT_URI, null,null, null, null);

        //Запускаем цикл обработчик для каждого контакта:
        if (cursor.getCount() > 0) {

            //Если значение имени и номера контакта больше 0 (то есть они существуют) выбираем
            //их значения в приложение привязываем с соответствующие поля "Имя" и "Номер":
            while (cursor.moveToNext()) {
                String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
                String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));

                //Получаем имя:
                if (hasPhoneNumber > 0) {
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null,
                            Phone_CONTACT_ID + " = ?", new String[] { contact_id }, null);

                    //и соответствующий ему номер:
                    while (phoneCursor.moveToNext()) {
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                        contacts.put(name,phoneNumber);
                    }
                }
            }

        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this.getApplicationContext(), ContactsActivity.class);
        intent.putExtra("Class", "ChooseContacts");
        intent.putExtra("Name", "");
        intent.putExtra("Number", "");
        startActivity(intent);
    }
}
