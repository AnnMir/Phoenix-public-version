package nsu.fit.g14201.marchenko.phoenix.recording;

import android.support.annotation.NonNull;

import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRepositoryException;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.VideoFragmentPath;

public interface VideoFragmentListener {
    void recordWillStart(VideoFragmentPath fragmentPath) throws RecordRepositoryException;

    void recordDidStart();

    void onFragmentSavedLocally(@NonNull String fragmentName);
}
