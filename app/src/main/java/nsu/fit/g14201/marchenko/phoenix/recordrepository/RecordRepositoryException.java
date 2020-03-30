package nsu.fit.g14201.marchenko.phoenix.recordrepository;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import androidx.annotation.IntDef;

public class RecordRepositoryException extends Throwable {
    public static final int RECORD_NOT_FOUND = 1;

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ RECORD_NOT_FOUND })
    public @interface RecordRepositoryError {}

    private final int reason;

    public RecordRepositoryException(@RecordRepositoryError int problem) {
        super(getDefaultMessage(problem));
        reason = problem;
    }

    public int getReason() {
        return reason;
    }

    public static String getDefaultMessage(@RecordRepositoryError int problem) {
        switch (problem) {
            case RECORD_NOT_FOUND:
                return "Haven't found such record in repository";
        }
        return null;
    }
}
