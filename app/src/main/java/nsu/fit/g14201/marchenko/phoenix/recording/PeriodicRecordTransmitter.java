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
import nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.CameraGLView;
import nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.LowLevelRecordingException;
import nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.encoding.AudioEncoder;
import nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.encoding.MediaEncoder;
import nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.encoding.MediaMuxerException;
import nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.encoding.MediaMuxerWrapper;
import nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.encoding.VideoEncoder;


class PeriodicRecordTransmitter {
    private CameraGLView cameraGLView;
    private MediaMuxerWrapper muxer; // FIXME: Move somewhere

    PeriodicRecordTransmitter(@NonNull CameraGLView cameraGLView) {
        this.cameraGLView = cameraGLView;
    }

    void start(MediaEncoder.MediaEncoderListener listener, Context context)
            throws LowLevelRecordingException, MediaMuxerException, CameraException, IOException {
        muxer = new MediaMuxerWrapper(createVideoPath(context));
        new VideoEncoder(muxer, cameraGLView.getVideoWidth(), cameraGLView.getVideoHeight(), listener);
        new AudioEncoder(muxer, listener);
        muxer.prepare();
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

    private String createVideoPath(Context context) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss");
        String videoDirectory = dateFormat.format(Calendar.getInstance().getTime());

        final File directory = new File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES),
                videoDirectory);
        if (!directory.mkdirs()) {
            Log.d(App.getTag(), "Failed to create directory for video"); // TODO: Error
        }
        String videoPath = directory.getAbsolutePath() + "/";

        return videoPath;
    }
}
