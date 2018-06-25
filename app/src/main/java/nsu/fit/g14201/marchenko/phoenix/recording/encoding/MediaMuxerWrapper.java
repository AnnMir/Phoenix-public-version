package nsu.fit.g14201.marchenko.phoenix.recording.encoding;


import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.model.VideoFragmentPath;
import nsu.fit.g14201.marchenko.phoenix.recording.camera.CameraException;
import nsu.fit.g14201.marchenko.phoenix.recording.gl.LowLevelRecordingException;

public class MediaMuxerWrapper {
    private static final boolean VERBOSE = true;

    private MediaMuxer muxer;
    private final VideoFragmentPath fragmentPath;
    private final String localStoragePath;
    private boolean muxerStarted = false;
    private int trackNum = 0;
    private int tracksStarted = 0;
    private boolean isFirstKeyframe = true;
    private SpecialFrameListener specialFrameListener;

    private MediaEncoder videoEncoder;
    private MediaAudioEncoder audioEncoder;

    public MediaMuxerWrapper(@NonNull VideoFragmentPath fragmentPath,
                             @NonNull String localStoragePath,
                             @NonNull SpecialFrameListener specialFrameListener)
            throws LowLevelRecordingException {
        this.fragmentPath = fragmentPath;
        this.localStoragePath = localStoragePath;
        this.specialFrameListener = specialFrameListener;
        try {
            fragmentPath.nextFragment();
            muxer = new MediaMuxer(fragmentPath.getCurrentFragmentPath(localStoragePath),
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
            throw new LowLevelRecordingException(LowLevelRecordingException.MEDIA_MUXER_INIT_ERROR);
        }
    }

    public synchronized void restart(int trackIndex,
                                     ByteBuffer byteBuffer,
                                     MediaCodec.BufferInfo bufferInfo)
            throws LowLevelRecordingException {
        muxer.stop();
        Log.d(App.getTag2(), "Muxer stopped");
        muxer.release();
        Log.d(App.getTag2(), "Muxer released");
        try {
            fragmentPath.nextFragment();
            muxer = new MediaMuxer(fragmentPath.getCurrentFragmentPath(localStoragePath),
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            Log.d(App.getTag2(), "New muxer created");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(App.getTag2(), "EXCEPTION!");
            throw new LowLevelRecordingException(LowLevelRecordingException.MEDIA_MUXER_INIT_ERROR);
        }
        int oldVideoRecorderTrackIndex = -1;
        if (videoEncoder != null) {
            oldVideoRecorderTrackIndex = videoEncoder.renewTrackIndex(
                    muxer.addTrack(videoEncoder.getOutputFormat()));
            Log.d(App.getTag2(), "Video renewed");

        }
        if (audioEncoder != null) {
            audioEncoder.renewTrackIndex(muxer.addTrack(audioEncoder.getOutputFormat()));
            Log.d(App.getTag2(), "Audio renewed");
        }

        muxer.start();
        Log.d(App.getTag2(), "Muxer started");

        if (oldVideoRecorderTrackIndex == trackIndex) {
            muxer.writeSampleData(videoEncoder.trackIndex, byteBuffer, bufferInfo);
            Log.d(App.getTag2(), "Muxer stopped");
        }
    }

    public void prepare() throws CameraException, IOException {
        if (videoEncoder != null) {
            videoEncoder.prepare();
        }
        if (audioEncoder != null) {
            audioEncoder.prepare();
        }
    }

    public void startRecording() {
        if (videoEncoder != null) {
            videoEncoder.startRecording();
        }
        if (audioEncoder != null) {
            audioEncoder.startRecording();
        }
    }

    public synchronized void stopRecording() {
        if (videoEncoder != null) {
            videoEncoder.stopRecording();
            videoEncoder = null;
        }
        if (audioEncoder != null) {
            audioEncoder.stopRecording();
            audioEncoder = null;
        }
    }

    public void removeSpecialFrameListener() {
        specialFrameListener = null;
    }

    void addEncoder(@NonNull VideoEncoder encoder) throws MediaMuxerException {
        if (videoEncoder != null) {
            throw new MediaMuxerException(MediaMuxerException.VIDEO_ENCODER_ALREADY_EXISTS);
        }
        videoEncoder = encoder;
        trackNum++;
    }

    void addEncoder(@NonNull AudioEncoder encoder) throws MediaMuxerException {
        if (audioEncoder != null) {
            throw new MediaMuxerException(MediaMuxerException.AUDIO_ENCODER_ALREADY_EXISTS);
        }
        audioEncoder = encoder;
        trackNum++;
    }

    /**
     * Assigns encoder to that muxer
     * @param format
     * @return Negative value indicates error
     */
    synchronized int addTrack(MediaFormat format) throws MediaMuxerException {
        if (muxerStarted) {
            throw new MediaMuxerException(MediaMuxerException.MUXER_ALREADY_STARTED);
        }
        int trackIndex = muxer.addTrack(format);
        if (VERBOSE) {
            Log.d(App.getTag2(), "Add track with number = " + trackIndex + " of " + trackNum +
                    ", format = " + format);
        }
        return trackIndex;
    }

    /**
     * @return true when muxer is ready to write
     */
	synchronized boolean start() {
        tracksStarted++;
        if ((trackNum > 0) && (tracksStarted == trackNum)) {
            muxer.start();
            muxerStarted = true;
        }

        return muxerStarted;
    }

    synchronized boolean hasStarted() {
        return muxerStarted;
    }

    /**
     * Writes encoded data to muxer
     * @param trackIndex
     * @param byteBuf
     * @param bufferInfo
     */
	synchronized void writeSampleData(int trackIndex, ByteBuffer byteBuf,
                                      MediaCodec.BufferInfo bufferInfo) {
	    Log.d(App.getTag2(), "writeSampleData: " + Integer.toString(trackIndex));
        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            new Handler(Looper.getMainLooper()).post(() -> specialFrameListener.onLastFrameReceived());
            return;
        }

        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0) {
            if (!isFirstKeyframe) {
                specialFrameListener.onKeyFrameReceived(trackIndex, byteBuf, bufferInfo);
                return;
            }
            isFirstKeyframe = false;
        }
        if (tracksStarted > 0) {
            muxer.writeSampleData(trackIndex, byteBuf, bufferInfo);
        }
    }

    synchronized void stop() {
        if (VERBOSE) {
            Log.d(App.getTag(),  "Stopped muxer: tracks started = " + tracksStarted);
        }

        tracksStarted--;
        if (trackNum > 0 && tracksStarted <= 0) {
            muxer.stop();
            muxer.release();
            muxerStarted = false;
            if (VERBOSE) {
                Log.d(App.getTag(),  "MediaMuxer stopped:");
            }
        }
    }

    public interface SpecialFrameListener {
	    void onKeyFrameReceived(int trackIndex, ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo);

	    void onLastFrameReceived();
	}
}
