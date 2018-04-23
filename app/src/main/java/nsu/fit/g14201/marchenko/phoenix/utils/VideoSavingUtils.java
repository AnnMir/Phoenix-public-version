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
    public static Uri getOutputMediaFileUri() throws SDCardNotReadyException {
        return Uri.fromFile(getOutputMediaFile());
    }

    /**
     * Create a File for saving  video
     */
    public static File getOutputMediaFile() throws SDCardNotReadyException {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            throw new SDCardNotReadyException(Environment.getExternalStorageState());
        }

//        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES), App.APP_NAME);


        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);

        Log.d(App.getTag(), path.toString());

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), App.getAppName());
        Log.d(App.getTag(), mediaStorageDir == null ? "null!" : "not null!");

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
