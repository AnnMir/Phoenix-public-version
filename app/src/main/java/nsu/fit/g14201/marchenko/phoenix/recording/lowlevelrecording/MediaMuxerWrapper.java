package nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording;


import android.media.MediaMuxer;

import java.io.IOException;

public class MediaMuxerWrapper {
    private final MediaMuxer muxer;
    private final String outputPath;
    private boolean muxerStarted = false;
    private int trackIndex;
    private int filenameIndex = 0;

    public MediaMuxerWrapper(String outputPath) throws LowLevelRecordingException {
        this.outputPath = outputPath;
        try {
            muxer = new MediaMuxer(getCurrentFilename(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            trackIndex = -1;
        } catch (IOException e) {
            throw new LowLevelRecordingException(LowLevelRecordingException.MEDIA_MUXER_INIT_ERROR);
        }
    }

    private String getCurrentFilename() {
        StringBuilder builder = new StringBuilder();
        builder.append(outputPath);
        builder.append(String.valueOf(filenameIndex++));
        builder.append(".mp4");

        return builder.toString();
    }
}
