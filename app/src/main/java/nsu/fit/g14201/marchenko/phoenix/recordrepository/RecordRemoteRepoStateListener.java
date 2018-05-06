package nsu.fit.g14201.marchenko.phoenix.recordrepository;

import android.support.annotation.NonNull;

public interface RecordRemoteRepoStateListener {
    void onFailedToCreateVideoRepository(@NonNull Exception e, @NonNull String name);
}
