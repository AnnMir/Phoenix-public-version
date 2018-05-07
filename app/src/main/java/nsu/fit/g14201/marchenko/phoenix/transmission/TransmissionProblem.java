package nsu.fit.g14201.marchenko.phoenix.transmission;


import android.support.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class TransmissionProblem {
    public static final int FAILED_TO_CREATE_VIDEO_FOLDER = 0;
    public static final int RECORD_NOT_FOUND_LOCALLY = 1;

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ FAILED_TO_CREATE_VIDEO_FOLDER,
            RECORD_NOT_FOUND_LOCALLY})
    public @interface TransmissionProblemType {}

    protected final int problem;

    public TransmissionProblem(@TransmissionProblemType int problem) {
        this.problem = problem;
    }

    public int getType() {
        return problem;
    }
}