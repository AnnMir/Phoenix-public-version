package nsu.fit.g14201.marchenko.phoenix.recording.camera;


import android.content.Context;
import android.media.MediaRecorder;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.WindowManager;

import java.io.IOException;

class VideoHandler {
    // FIXME: Could be turned into static variables, but should it be so?
    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();

    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    private MediaRecorder mediaRecorder;
    private Size videoSize;
    private String videoPath;
    private Integer sensorOrientation;
    private int orientationHint;
    private int fragmentNumber = 0;

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

    void setUpRecorder(Context context, String videoPath) throws IOException {
        // TODO: Make RECORD_AUDIO permission necessary

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            return;
        }
        int rotation = windowManager.getDefaultDisplay().getRotation();
        switch (sensorOrientation) {
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                orientationHint = DEFAULT_ORIENTATIONS.get(rotation);
                break;
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                orientationHint = INVERSE_ORIENTATIONS.get(rotation);
                break;
        }
        this.videoPath = videoPath;
        setUpRecorder();
    }

    void setUpRecorder() throws IOException {
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        mediaRecorder.setOutputFile(String.format("%s%d.mp4", videoPath, fragmentNumber++));
        mediaRecorder.setVideoEncodingBitRate(10000000);
        mediaRecorder.setVideoFrameRate(60);
        mediaRecorder.setVideoSize(videoSize.getWidth(), videoSize.getHeight());
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOrientationHint(orientationHint);

        mediaRecorder.prepare();
    }

    void startRecording() {
        mediaRecorder.start();
    }

    void stopRecording() {
        mediaRecorder.stop();
    }

    void resetRecorder() {
        fragmentNumber = 0;
        mediaRecorder.reset();
    }

    void closeRecorder() {
        mediaRecorder.release();
    }
}
