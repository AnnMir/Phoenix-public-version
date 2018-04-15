package nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.encoding;


import android.media.MediaMuxer;
import android.support.annotation.NonNull;

import java.io.IOException;

import nsu.fit.g14201.marchenko.phoenix.recording.camera.CameraException;
import nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.LowLevelRecordingException;

public class MediaMuxerWrapper {
    private final MediaMuxer muxer;
    private final String outputPath;
    private boolean muxerStarted = false;
    private int trackIndex;
    private int filenameIndex = 0;

    private MediaEncoder videoEncoder;
    private MediaEncoder audioEncoder;

    public MediaMuxerWrapper(@NonNull String outputPath) throws LowLevelRecordingException {
        this.outputPath = outputPath;
        try {
            muxer = new MediaMuxer(getCurrentFilename(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            trackIndex = -1;
        } catch (IOException e) {
            e.printStackTrace();
            throw new LowLevelRecordingException(LowLevelRecordingException.MEDIA_MUXER_INIT_ERROR);
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

    void addEncoder(@NonNull VideoEncoder encoder) throws MediaMuxerException {
        if (videoEncoder != null) {
            throw new MediaMuxerException(MediaMuxerException.VIDEO_ENCODER_ALREADY_EXISTS);
        }
        videoEncoder = encoder;
    }

    void addEncoder(@NonNull AudioEncoder encoder) throws MediaMuxerException {
        if (audioEncoder != null) {
            throw new MediaMuxerException(MediaMuxerException.AUDIO_ENCODER_ALREADY_EXISTS);
        }
        audioEncoder = encoder;
    }

    // TODO: remove
    public void release() {
        videoEncoder.release();
        audioEncoder.release();
    }

    private String getCurrentFilename() {
        StringBuilder builder = new StringBuilder();
        builder.append(outputPath);
        builder.append(String.valueOf(filenameIndex++));
        builder.append(".mp4");

        return builder.toString();
    }
}
