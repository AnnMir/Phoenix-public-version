package nsu.fit.g14201.marchenko.phoenix.recording.camera;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.support.v4.content.ContextCompat;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;

class VideoHandler {
    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();

    private MediaRecorder mediaRecorder;
    private String nextVideoAbsolutePath;
    private Size videoSize;
    private Integer sensorOrientation;

    VideoHandler() {
        mediaRecorder = new MediaRecorder();
    }

    void setVideoSize(Size videoSize) {
        this.videoSize = videoSize;
    }

    void setSensorOrientation(Integer sensorOrientation) {
        this.sensorOrientation = sensorOrientation;
    }

    Surface getSurface() {
        return mediaRecorder.getSurface();
    }

    void setUpRecorder(Context context) throws IOException {

        boolean canContainAudioTrack = ContextCompat.checkSelfPermission(context,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        if (canContainAudioTrack) {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        }
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        if (nextVideoAbsolutePath == null || nextVideoAbsolutePath.isEmpty()) {
            nextVideoAbsolutePath = getVideoFilePath(context);
        }
        mediaRecorder.setOutputFile(nextVideoAbsolutePath);
        mediaRecorder.setVideoEncodingBitRate(10000000);
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setVideoSize(videoSize.getWidth(), videoSize.getHeight());
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        if (canContainAudioTrack) {
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        }

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            return;
        }
        int rotation = windowManager.getDefaultDisplay().getRotation();
        switch (sensorOrientation) {
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                mediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
                break;
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                mediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
                break;
        }
        mediaRecorder.prepare();
    }

    void startRecording() {
        mediaRecorder.start();
    }

    void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.reset();
    }

    String getAndResetLastVideoPath() {
        String path = nextVideoAbsolutePath;
        nextVideoAbsolutePath = null;
        return path;
    }

    void closeRecorder() {
        mediaRecorder.release();
    }

    private String getVideoFilePath(Context context) {
        final File dir = context.getExternalFilesDir(null);
        return (dir == null ? "" : (dir.getAbsolutePath() + "/"))
                + System.currentTimeMillis() + ".mp4";
    }
}
