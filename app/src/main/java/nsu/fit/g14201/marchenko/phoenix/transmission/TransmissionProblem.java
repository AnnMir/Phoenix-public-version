package nsu.fit.g14201.marchenko.phoenix.transmission;




import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import androidx.annotation.IntDef;

public class TransmissionProblem {
    public static final int UNKNOWN = 0;
    public static final int FAILED_TO_CREATE_VIDEO_FOLDER = 1;
    public static final int RECORD_NOT_FOUND_LOCALLY = 2;

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
