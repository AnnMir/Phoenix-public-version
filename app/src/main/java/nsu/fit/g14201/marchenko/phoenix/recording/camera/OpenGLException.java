package nsu.fit.g14201.marchenko.phoenix.recording.camera;


import android.support.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class OpenGLException extends Throwable {
    public static final int STANDARD_ERROR = 0;
    public static final int CAN_NOT_GET_EGL_DISPLAY = 1;
    public static final int EGL_DISPLAY_INIT_ERROR = 2;

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STANDARD_ERROR, CAN_NOT_GET_EGL_DISPLAY, EGL_DISPLAY_INIT_ERROR})
    public @interface OpenGLError {}

    private final int reason;
    private final String details;

    public OpenGLException(int error, String message) {
        reason = STANDARD_ERROR;
        details = message + ": EGL error: 0x" + Integer.toHexString(error);
    }

    public OpenGLException(@OpenGLError int problem) {
        super();
        reason = problem;
        details = null;
    }

    @Override
    public String getMessage() {
        switch (reason) {
            case STANDARD_ERROR:
                return details;
            case CAN_NOT_GET_EGL_DISPLAY:
                return "Unable to get EGL14 display";
            case EGL_DISPLAY_INIT_ERROR:
                return "Unable to initialize EGL14";
        }
        return null;
    }
}
