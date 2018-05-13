package nsu.fit.g14201.marchenko.phoenix.model;

import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RecordPath {
    private final String directoryName;

    public RecordPath(@NonNull String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern.toString());
        directoryName = dateFormat.format(Calendar.getInstance().getTime());
    }

    public String getDirectoryName() {
        return directoryName;
    }
}
