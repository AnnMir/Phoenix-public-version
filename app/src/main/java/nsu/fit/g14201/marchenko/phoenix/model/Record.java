package nsu.fit.g14201.marchenko.phoenix.model;

import android.support.annotation.NonNull;

import java.io.File;

public class Record {
    private File path;

    public Record(@NonNull File path) {
        this.path = path;
    }
}
