package nsu.fit.g14201.marchenko.phoenix.recording;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import nsu.fit.g14201.marchenko.phoenix.App;

public class VideoFragmentPath {
    private final String fullDirectoryPath;
    private int filenameIndex = -1;

    public VideoFragmentPath(Context context) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss");
        String videoDirectory = dateFormat.format(Calendar.getInstance().getTime());

        final File directory = new File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES),
                videoDirectory);
        if (!directory.mkdirs()) {
            Log.d(App.getTag(), "Failed to create directory for video"); // TODO: Error
        }
        fullDirectoryPath = directory.getAbsolutePath() + "/";
    }

    public void nextFragment() {
        filenameIndex++;
    }

    public String getFullDirectoryPath() {
        return fullDirectoryPath;
    }

    public String getShortDirectoryPath() {
        String[] pathParts = fullDirectoryPath.split("/");
        return pathParts[pathParts.length - 1];
    }

    public String getFullPath() {
        StringBuilder builder = new StringBuilder();
        builder.append(fullDirectoryPath);
        builder.append(String.valueOf(filenameIndex));
        builder.append(".mp4");

        return builder.toString();
    }
}
