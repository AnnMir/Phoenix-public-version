package nsu.fit.g14201.marchenko.phoenix.recordrepository;

import android.support.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class RecordRepositoryException extends Throwable {
    public static final int DIRECTORY_CREATION_ERROR = 0;

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ DIRECTORY_CREATION_ERROR })
    public @interface RecordRepositoryError {}

    private final int reason;

    public RecordRepositoryException(@RecordRepositoryError int problem) {
        super(getDefaultMessage(problem));
        reason = problem;
    }

    public static String getDefaultMessage(@RecordRepositoryError int problem) {
        switch (problem) {
            case DIRECTORY_CREATION_ERROR:
                return "Failed to create directory";
        }
        return null;
    }
}
