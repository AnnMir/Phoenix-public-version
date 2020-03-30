package nsu.fit.g14201.marchenko.phoenix.model;



import java.io.File;

import androidx.annotation.NonNull;

public class VideoFragmentPath {
    private final RecordPath recordPath;
    private final String extension;
    private int filenameIndex = -1;

    public VideoFragmentPath(@NonNull RecordPath recordPath, @NonNull String extension) {
        this.recordPath = recordPath;
        this.extension = extension;
    }

    public void nextFragment() {
        filenameIndex++;
    }

    public String getDirectoryName() {
        return recordPath.getDirectoryName();
    }

    public String getFullDirectoryName(@NonNull String appStoragePath) {
        return appStoragePath + recordPath.getDirectoryName();
    }

    public String getRelativeNameByFragmentNumber(int fragmentNumber) {
        StringBuilder builder = new StringBuilder();

        builder.append(recordPath.getDirectoryName());
        builder.append(File.separator);
        builder.append(String.valueOf(fragmentNumber));
        builder.append(extension);

        return builder.toString();
    }

    public String getFragmentFileNameByNumber(int fragmentNumber) {
        return String.valueOf(fragmentNumber) + extension;
    }

    public int getCurrentFragmentNumber() {
        return filenameIndex;
    }

    public String getCurrentFragmentPath(String appStoragePath) {
        StringBuilder builder = new StringBuilder();

        builder.append(appStoragePath);
        builder.append(recordPath.getDirectoryName());
        builder.append(File.separator);
        builder.append(String.valueOf(filenameIndex));
        builder.append(extension);

        return builder.toString();
    }
}
