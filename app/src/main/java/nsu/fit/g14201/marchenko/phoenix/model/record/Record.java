package nsu.fit.g14201.marchenko.phoenix.model.record;

import android.support.annotation.NonNull;

import java.io.File;

public class Record {
    private File path;

    public String title;
    public String date;
    public boolean fromCloud = false;

    public Record() {}

    public String getTitle2() {
        return title;
    }

    public Record(@NonNull File path) {
        this.path = path;
    }

    public String getTitle() {
        return path.getName();
    }

    public File getPath() {
        return path;
    }
}
