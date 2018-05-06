package nsu.fit.g14201.marchenko.phoenix.recording;

import android.support.annotation.NonNull;

import nsu.fit.g14201.marchenko.phoenix.recordrepository.VideoFragmentPath;

public interface VideoFragmentListener {
    void recordWillStart(VideoFragmentPath fragmentPath);

    void recordDidStart();

    void onFragmentSavedLocally(@NonNull String fragmentName);
}
