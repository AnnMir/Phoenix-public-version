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
import nsu.fit.g14201.marchenko.phoenix.recording.camera.CameraStateListener;
import nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.CameraGLView;
import nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.LowLevelRecordingException;
import nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.encoding.MediaEncoder;
import nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.encoding.MediaMuxerException;
import nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.encoding.MediaMuxerWrapper;
import nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.encoding.VideoEncoder;

// TODO IMPORTANT: Отследить поведение при повороте

class PeriodicRecordTransmitter {
    private CameraGLView cameraGLView;
    private MediaMuxerWrapper muxer; // FIXME: Move somewhere
    private String videoPath;
    private CameraStateListener cameraStateListener;

    PeriodicRecordTransmitter(@NonNull CameraGLView cameraGLView,
                              @NonNull CameraStateListener cameraStateListener) {
        this.cameraGLView = cameraGLView;
        this.cameraStateListener = cameraStateListener;
    }

    void start(MediaEncoder.MediaEncoderListener mediaEncoderListener, Context context)
            throws LowLevelRecordingException, MediaMuxerException, CameraException, IOException {
        createVideoPath(context);
        muxer = new MediaMuxerWrapper(videoPath);
        new VideoEncoder(muxer, cameraGLView.getVideoWidth(), cameraGLView.getVideoHeight(),
                mediaEncoderListener);
//        new AudioEncoder(muxer, listener); // TODO: Audio track
        muxer.prepare();
        muxer.startRecording();
        cameraStateListener.onRecordingStarted();
    }

    void stop() {
        if (muxer != null) {
            muxer.stopRecording();
            muxer = null;
            cameraStateListener.onRecordingFinished(videoPath);
        }
    }

    void pause() {
        cameraGLView.onPause();
    }

    void resume() throws CameraAccessException {
        cameraGLView.onResume();
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
