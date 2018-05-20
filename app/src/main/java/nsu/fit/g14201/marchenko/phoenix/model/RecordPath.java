package nsu.fit.g14201.marchenko.phoenix.model;

import android.support.annotation.NonNull;

public class RecordPath {
    private final String directoryName;

    public RecordPath(@NonNull String directoryName) {
        this.directoryName = directoryName;
    }

    public String getDirectoryName() {
        return directoryName;
    }
}
