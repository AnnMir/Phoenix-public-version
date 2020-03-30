package nsu.fit.g14201.marchenko.phoenix.recordrepository;


import androidx.annotation.NonNull;

public interface RecordRemoteRepoStateListener {
    void onFailedToCreateAppRepository();

    void onFailedToCreateVideoRepository(@NonNull Throwable throwable, @NonNull String name);
}
