package nsu.fit.g14201.marchenko.phoenix.recording;



import androidx.annotation.NonNull;
import nsu.fit.g14201.marchenko.phoenix.model.VideoFragmentPath;

public interface RecordingListener {
    void recordWillStart(@NonNull VideoFragmentPath videoFragmentPath);

    void recordDidStart();
}
