package nsu.fit.g14201.marchenko.phoenix.recording;


import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.recording.camera.CameraHandler;


class PeriodicRecordTransmitter {
    private CameraHandler cameraHandler;
    private String videoPath;

    PeriodicRecordTransmitter(@NonNull CameraHandler cameraHandler) {
        this.cameraHandler = cameraHandler;
    }

    void start(Context context) {
        startRecording(context);
        cameraHandler.startRecording(videoPath);
    }

    void stop() {
        cameraHandler.stopRecording(videoPath);
    }

    void pause() {
        cameraHandler.closeCamera();
    }

    void resume() throws CameraAccessException {
        cameraHandler.resumeCameraWork();
    }

    private void startRecording(Context context) {
        createVideoPath(context);
    }

    private void createVideoPath(Context context) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmmss");
        String videoDirectory = dateFormat.format(Calendar.getInstance().getTime());

        final File directory = new File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES),
                videoDirectory);
        if (!directory.mkdirs()) {
            Log.d(App.getTag(), "Failed to create directory for video"); // TODO: Error
        }
        videoPath = directory.getAbsolutePath() + "/";
    }
}
