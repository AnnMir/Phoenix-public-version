package nsu.fit.g14201.marchenko.phoenix.utils;


import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import nsu.fit.g14201.marchenko.phoenix.App;

public class VideoSavingUtils {
    private VideoSavingUtils() {
    }

    /**
     * Create a file Uri for saving video
     */
    public static Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
    }

    /**
     * Create a File for saving  video
     */
    public static File getOutputMediaFile() {
        //TODO
        // To be safe, check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), App.APP_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(App.getTag(), "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "VID_" + timeStamp + ".mp4");
    }
}
