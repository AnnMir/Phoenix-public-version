package nsu.fit.g14201.marchenko.phoenix.contacts;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import nsu.fit.g14201.marchenko.phoenix.contacts.ContactsContract.ContactsEntry;

public class ContactsDBHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Contacts.db";

    public ContactsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_ENTRIES = "CREATE TABLE " + ContactsEntry.TABLE_NAME + " (" +
                ContactsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ContactsEntry.EMAIL + " TEXT NOT NULL," +
                ContactsEntry.NAME + " TEXT NOT NULL," +
                ContactsEntry.NUMBER + " TEXT NOT NULL)";
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS " + ContactsEntry.TABLE_NAME);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}