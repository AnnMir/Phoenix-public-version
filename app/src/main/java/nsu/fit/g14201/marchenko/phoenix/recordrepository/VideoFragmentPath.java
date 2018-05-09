package nsu.fit.g14201.marchenko.phoenix.recordrepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class VideoFragmentPath {
    private final String directoryName;
    private final String extension = ".mp4";
    private int filenameIndex = -1;

    public VideoFragmentPath() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss");
        directoryName = dateFormat.format(Calendar.getInstance().getTime());
    }

    public void nextFragment() {
        filenameIndex++;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public String getRelativeNameByFragmentNumber(int fragmentNumber) {
        StringBuilder builder = new StringBuilder();

        builder.append(directoryName);
        builder.append("/");
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
        builder.append(directoryName);
        builder.append("/");
        builder.append(String.valueOf(filenameIndex));
        builder.append(extension);

        return builder.toString();
    }
}
