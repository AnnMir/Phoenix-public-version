package nsu.fit.g14201.marchenko.phoenix.contacts;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.connection.GoogleUserConnection;
import nsu.fit.g14201.marchenko.phoenix.contacts.adapter.ContactsAdapter;


public class ContactsActivity extends AppCompatActivity {
    private static Map<String, String> contacts;
    private static ContactsDBController controller;
    private RecyclerView recyclerView;
    private ContactsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        controller = new ContactsDBController(this.getApplicationContext());
        String classname = getIntent().getStringExtra("Class");

        if(classname.equals("ChooseContacts")){
            String newName = getIntent().getStringExtra("Name");
            String newNumber = getIntent().getStringExtra("Number");
            controller.addContact(GoogleUserConnection.getInstance().getCredential().getSelectedAccountName(),newName, newNumber);
        }
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
            @Override public void onItemClick(View view, int position) {}

            @Override public void onLongItemClick(View view, int position) {
                AlertDialog.Builder alert = new android.app.AlertDialog.Builder(view.getContext());
                alert.setTitle("Удаление контакта");
                alert.setMessage("Вы действительно хотите удалить контакт из списка?");
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteContact(GoogleUserConnection.getInstance().getCredential().getSelectedAccountName(), adapter.getNumber(position));
                        dialog.dismiss();
                    }
                });

                alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.show();
            }
        }));
    }

    private void loadContacts() {
        //получить номера
        contacts = controller.getContacts(GoogleUserConnection.getInstance().getCredential().getSelectedAccountName());
        adapter.setItems(contacts);
    }

    public void deleteContact(String email, String number){
        controller.deleteContact(email, number);
        contacts = controller.getContacts(email);
        loadContacts();
    }

    public void choose(View view){
        Intent intent = new Intent(this, ChooseContacts.class);
        startActivity(intent);
    }
}
