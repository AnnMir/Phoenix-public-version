package nsu.fit.g14201.marchenko.phoenix.recording;


import android.support.annotation.NonNull;

import nsu.fit.g14201.marchenko.phoenix.recordrepository.VideoFragmentPath;

public interface RecordingListener {
    void recordWillStart(@NonNull VideoFragmentPath videoFragmentPath);

    void recordDidStart();

    void recordDidStop();
}
