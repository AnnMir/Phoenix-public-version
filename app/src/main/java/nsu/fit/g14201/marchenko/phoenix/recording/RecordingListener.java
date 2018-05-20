package nsu.fit.g14201.marchenko.phoenix.recording;


import android.support.annotation.NonNull;

import nsu.fit.g14201.marchenko.phoenix.model.VideoFragmentPath;

public interface RecordingListener {
    void recordWillStart(@NonNull VideoFragmentPath videoFragmentPath);

    void recordDidStart();
}
