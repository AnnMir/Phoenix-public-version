package nsu.fit.g14201.marchenko.phoenix.contacts;

import android.provider.BaseColumns;

public final class ContactsContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private ContactsContract() {
    }

    /* Inner class that defines the table contents */
    public static class ContactsEntry implements BaseColumns {
        public static final String TABLE_NAME = "contacts";

        public static final String _ID = BaseColumns._ID;
        public static final String EMAIL = "Email";
        public static final String NAME = "Name";
        public static final String NUMBER = "Number";
    }
}
