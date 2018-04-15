package nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.encoding;


import android.support.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class MediaMuxerException extends Throwable {
    public static final int AUDIO_ENCODER_ALREADY_EXISTS = 0;
    public static final int VIDEO_ENCODER_ALREADY_EXISTS = 1;

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ AUDIO_ENCODER_ALREADY_EXISTS,
            VIDEO_ENCODER_ALREADY_EXISTS})
    public @interface MediaMuxerError {}

    private final int reason;

    public MediaMuxerException(@MediaMuxerError int problem) {
        super();
        reason = problem;
    }

    @Override
    public String getMessage() {
        switch (reason) {
            case AUDIO_ENCODER_ALREADY_EXISTS:
                return "Audio encoder can not be replaced";
            case VIDEO_ENCODER_ALREADY_EXISTS:
                return "Video encoder can not be replaced";
        }
        return null;
    }
}
