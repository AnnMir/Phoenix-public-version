package nsu.fit.g14201.marchenko.phoenix.recording.camera;


import android.support.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class CameraException extends Throwable {
    public static final int NO_CAMERAS_FOUND = 0;
    public static final int NO_CODEC_FOUND = 1;

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NO_CAMERAS_FOUND, NO_CODEC_FOUND})
    public @interface CameraError {}

    private final int reason;

    public CameraException(@CameraError int problem) {
        super(getDefaultMessage(problem));
        reason = problem;
    }

    public static String getDefaultMessage(@CameraError int problem) {
        switch (problem) {
            case NO_CAMERAS_FOUND:
                return "Haven't found neither back nor front camera";
            case NO_CODEC_FOUND:
                return "Haven't found codec satisfying given format";
        }
        return null;
    }
}
