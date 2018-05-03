package nsu.fit.g14201.marchenko.phoenix.recordrepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class VideoFragmentPath {
    private final String directoryName;
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

    public String getFullFilePath(String appStoragePath) {
        StringBuilder builder = new StringBuilder();
        builder.append(appStoragePath);
        builder.append(directoryName);
        builder.append("/");
        builder.append(String.valueOf(filenameIndex));
        builder.append(".mp4");

        return builder.toString();
    }
}
