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
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRepositoryException;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.VideoFragmentPath;

// TODO IMPORTANT: Отследить поведение при повороте

class PeriodicFragmentRecorder implements MediaMuxerWrapper.KeyFrameListener, Contextual {
    private CameraGLView cameraGLView;
    private MediaMuxerWrapper muxer;

    private nsu.fit.g14201.marchenko.phoenix.context.Context context;
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
            muxer.restart(trackIndex, byteBuffer, bufferInfo);
            fragmentListener.onFragmentSavedLocally();
        } catch (LowLevelRecordingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setContext(nsu.fit.g14201.marchenko.phoenix.context.Context context) {
        this.context = context;
    }

    void start(@NonNull MediaEncoder.MediaEncoderListener mediaEncoderListener,
               @NonNull VideoFragmentListener videoFragmentListener)
            throws LowLevelRecordingException, MediaMuxerException, CameraException, IOException,
            RecordRepositoryException {
        videoFragmentPath = new VideoFragmentPath();
        fragmentListener = videoFragmentListener;
        fragmentListener.recordWillStart(videoFragmentPath);

        muxer = new MediaMuxerWrapper(videoFragmentPath,
                this.context.getRecordRepositoriesController().getLocalStoragePath(),
                this);
        new VideoEncoder(muxer, cameraGLView.getVideoWidth(), cameraGLView.getVideoHeight(),
                mediaEncoderListener);
//        new AudioEncoder(muxer, listener); // TODO: Audio track
        muxer.prepare();
        muxer.startRecording();

        fragmentListener.recordDidStart();
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
            cameraStateListener.onRecordingFinished(videoFragmentPath.getFullFilePath(
                    context.getRecordRepositoriesController().getLocalStoragePath()));
            fragmentListener = null;
        }
    }

    void pause() {
        cameraGLView.onPause();
    }

    void resume() {
        cameraGLView.onResume();
    }
}
