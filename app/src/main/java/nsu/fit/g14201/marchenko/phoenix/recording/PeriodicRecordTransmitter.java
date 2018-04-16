package nsu.fit.g14201.marchenko.phoenix.recording;


import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.media.MediaCodec;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.recording.camera.CameraException;
import nsu.fit.g14201.marchenko.phoenix.recording.camera.CameraStateListener;
import nsu.fit.g14201.marchenko.phoenix.recording.encoding.MediaEncoder;
import nsu.fit.g14201.marchenko.phoenix.recording.encoding.MediaMuxerException;
import nsu.fit.g14201.marchenko.phoenix.recording.encoding.MediaMuxerWrapper;
import nsu.fit.g14201.marchenko.phoenix.recording.encoding.VideoEncoder;
import nsu.fit.g14201.marchenko.phoenix.recording.gl.CameraGLView;
import nsu.fit.g14201.marchenko.phoenix.recording.gl.LowLevelRecordingException;

// TODO IMPORTANT: Отследить поведение при повороте

class PeriodicRecordTransmitter implements MediaMuxerWrapper.KeyFrameListener {
    private CameraGLView cameraGLView;
    private MediaMuxerWrapper muxer; // FIXME: Move somewhere
    private String videoPath;
    private CameraStateListener cameraStateListener;

    PeriodicRecordTransmitter(@NonNull CameraGLView cameraGLView,
                              @NonNull CameraStateListener cameraStateListener) {
        this.cameraGLView = cameraGLView;
        this.cameraStateListener = cameraStateListener;
    }

    @Override
    public void onKeyFrameReceived(int trackIndex, ByteBuffer byteBuffer,
                                   MediaCodec.BufferInfo bufferInfo) {
        try {
            muxer.restart(trackIndex, byteBuffer, bufferInfo);
        } catch (LowLevelRecordingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    void start(MediaEncoder.MediaEncoderListener mediaEncoderListener, Context context)
            throws LowLevelRecordingException, MediaMuxerException, CameraException, IOException {
        createVideoPath(context);
        muxer = new MediaMuxerWrapper(videoPath, this);
        new VideoEncoder(muxer, cameraGLView.getVideoWidth(), cameraGLView.getVideoHeight(),
                mediaEncoderListener);
//        new AudioEncoder(muxer, listener); // TODO: Audio track
        muxer.prepare();
        muxer.startRecording();
        cameraStateListener.onRecordingStarted();
    }

//    private void startTimer() {
//        disposable = Observable.interval(timerPeriod, timerPeriod, TimeUnit.SECONDS)
//                .observeOn(Schedulers.io())
//                .subscribe(counter -> {
//                    try {
//                        saveFragment();
//                    } catch (LowLevelRecordingException e) {
//                        e.printStackTrace();
//                        throw new RuntimeException(e);
//                    }
//                });
//    }

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
