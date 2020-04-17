package nsu.fit.g14201.marchenko.phoenix.contacts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.contacts.ContactsContract.ContactsEntry;

import java.util.HashMap;
import java.util.Map;

public class ContactsDBController {
    private ContactsDBHelper dataBaseHelper;

    public ContactsDBController(Context context) {
        dataBaseHelper = new ContactsDBHelper(context);
    }

    public Map<String, String> getContacts(String email){
        Map<String, String> map = new HashMap<>();
        String selection;
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        String[] projection = {
                ContactsEntry.NAME, ContactsEntry.NUMBER};
        String[] selectionArgs;
        selection = ContactsEntry.EMAIL + "=?";
        selectionArgs = new String[]{email};
        Cursor cursor = db.query(
                ContactsEntry.TABLE_NAME, // таблица
                projection,            // столбцы
                selection,             // столбцы для условия WHERE
                selectionArgs,         // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                ContactsEntry.NAME + " ASC");  // порядок сортировки
        try {
            while (cursor.moveToNext()) {
                String currentName = cursor.getString(cursor.getColumnIndex(ContactsEntry.NAME));
                String currentNumber = cursor.getString(cursor.getColumnIndex(ContactsEntry.NUMBER));
                map.put(currentName, currentNumber);
            }
        } finally {
            cursor.close();
        }
        return map;
    }

    /**
     * Функция добавления контакта пользователя в базу данных
     * @param email
     * @param name
     * @param number
     */

    public void addContact(String email, String name, String number){
        if(name != "" && number != "" && email != "")
            doInsert(email, name, number);
    }

    public void deleteContact(String email, String number){
        if(!email.equals("") && !number.equals(""))
            delete(number, email);
    }

    private void doInsert(String email, String name, String number) {
        SQLiteDatabase db = dataBaseHelper.getWritableDatabase();

        //SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        //String currentDay = sdf.format(Calendar.getInstance().getTime());

        ContentValues values = new ContentValues();
        values.put(ContactsEntry.EMAIL, email);
        values.put(ContactsEntry.NAME, name);
        values.put(ContactsEntry.NUMBER, number);

        db.insert(ContactsEntry.TABLE_NAME, null, values);
        Log.d(App.getTag(), "added");
    }

    /**
     * Функция удаления записки пользователя из базы данных
     *
     * @param number
     * @param email
     */

    private void delete(String number, String email) {
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        db.delete(ContactsEntry.TABLE_NAME, "Email=? and Number=?", new String[]{email,number});
        Log.d(App.getTag(), "deleted");
    }
}
