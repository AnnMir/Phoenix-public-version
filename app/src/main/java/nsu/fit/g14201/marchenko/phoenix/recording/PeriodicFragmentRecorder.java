package nsu.fit.g14201.marchenko.phoenix.recording;


import android.media.MediaCodec;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;

import nsu.fit.g14201.marchenko.phoenix.context.Contextual;
import nsu.fit.g14201.marchenko.phoenix.recording.camera.CameraException;
import nsu.fit.g14201.marchenko.phoenix.recording.camera.CameraStateListener;
import nsu.fit.g14201.marchenko.phoenix.recording.encoding.MediaEncoder;
import nsu.fit.g14201.marchenko.phoenix.recording.encoding.MediaMuxerException;
import nsu.fit.g14201.marchenko.phoenix.recording.encoding.MediaMuxerWrapper;
import nsu.fit.g14201.marchenko.phoenix.recording.encoding.VideoEncoder;
import nsu.fit.g14201.marchenko.phoenix.recording.gl.CameraGLView;
import nsu.fit.g14201.marchenko.phoenix.recording.gl.LowLevelRecordingException;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.VideoFragmentPath;

class PeriodicFragmentRecorder implements MediaMuxerWrapper.SpecialFrameListener, Contextual {
    private CameraGLView cameraGLView;
    private MediaMuxerWrapper muxer;

    private nsu.fit.g14201.marchenko.phoenix.context.Context appContext;
    private VideoFragmentPath videoFragmentPath;
    private CameraStateListener cameraStateListener;
    private VideoFragmentListener fragmentListener;

    PeriodicFragmentRecorder(@NonNull CameraGLView cameraGLView,
                             @NonNull CameraStateListener cameraStateListener) {
        this.cameraGLView = cameraGLView;
        this.cameraStateListener = cameraStateListener;
    }

    @Override
    public void onKeyFrameReceived(int trackIndex, ByteBuffer byteBuffer,
                                   MediaCodec.BufferInfo bufferInfo) {
        try {
            int currentFragmentNum = videoFragmentPath.getCurrentFragmentNumber();
            muxer.restart(trackIndex, byteBuffer, bufferInfo);
            if (fragmentListener != null) {
                fragmentListener.onFragmentSavedLocally(currentFragmentNum);
            }
        } catch (LowLevelRecordingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onLastFrameReceived() {
        muxer.removeSpecialFrameListener();
        muxer = null;
        fragmentListener.onLastFragmentSaved(videoFragmentPath.getCurrentFragmentNumber());
        cameraStateListener.onRecordingFinished(
                videoFragmentPath.getFullDirectoryName(
                        appContext.getLocalStorage().getPath()
                )
        );
    }

    @Override
    public void setContext(nsu.fit.g14201.marchenko.phoenix.context.Context context) {
        this.appContext = context;
    }

    void start(@NonNull MediaEncoder.MediaEncoderListener mediaEncoderListener,
               @NonNull VideoFragmentPath videoFragmentPath)
            throws LowLevelRecordingException, MediaMuxerException, CameraException, IOException {
        this.videoFragmentPath = videoFragmentPath;
        muxer = new MediaMuxerWrapper(videoFragmentPath,
                this.appContext.getLocalStorage().getPath(),
                this);
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
        }
    }

    void pause() {
        cameraGLView.onPause();
    }

    void resume() {
        cameraGLView.onResume();
    }

    void setVideoFragmentListener(@NonNull VideoFragmentListener listener) {
        fragmentListener = listener;
    }

    void removeVideoFragmentListener() { // TODO: Use
        fragmentListener = null;
    }

    void removeCameraStateListener() {
        cameraStateListener = null;
    }
}
