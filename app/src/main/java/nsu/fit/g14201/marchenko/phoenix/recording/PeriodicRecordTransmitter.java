package nsu.fit.g14201.marchenko.phoenix.recording;


import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.recording.camera.CameraException;
import nsu.fit.g14201.marchenko.phoenix.recording.camera.CameraHandler;
import nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.CameraGLView;
import nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.LowLevelRecordingException;
import nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.LowLevelVideoHandler;


class PeriodicRecordTransmitter {
    private CameraGLView cameraGLView;
    private String videoPath;

    PeriodicRecordTransmitter(@NonNull CameraGLView cameraGLView) {
        this.cameraGLView = cameraGLView;
    }

    void start(Context context) {

//        startRecording(context);
//        cameraHandler.startRecording(videoPath);
    }

    void stop() {
//        cameraHandler.stopRecording(videoPath);
    }

    void pause() {
        cameraGLView.onPause();
    }

    void resume() throws CameraAccessException {
        cameraGLView.onResume();
    }

    private void startRecording(Context context) {
//        createVideoPath(context);
    }

    private void createVideoPath(Context context) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss");
        String videoDirectory = dateFormat.format(Calendar.getInstance().getTime());

        final File directory = new File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES),
                videoDirectory);
        if (!directory.mkdirs()) {
            Log.d(App.getTag(), "Failed to create directory for video"); // TODO: Error
        }
        videoPath = directory.getAbsolutePath() + "/";
    }
}
