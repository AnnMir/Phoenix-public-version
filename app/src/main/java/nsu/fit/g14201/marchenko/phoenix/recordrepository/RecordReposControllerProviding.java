package nsu.fit.g14201.marchenko.phoenix.recordrepository;


import android.support.annotation.NonNull;

import java.io.FileInputStream;

import nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice.CloudService;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice.CloudServiceListener;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage.LocalStorageListener;

public interface RecordReposControllerProviding extends LocalStorageListener, CloudServiceListener {
    void addCloudService(@NonNull CloudService cloudService);

    void createVideoRepositoryLocally(@NonNull String repositoryName);

    void createVideoRepositoryRemotely(@NonNull String repositoryName);

    String getLocalStoragePath();

    void setLocalRepoStateListener(@NonNull RecordLocalRepoStateListener localRepoListener);

    void removeLocalRepoStateListener();

    void setRemoteRepoStateListener(@NonNull RecordRemoteRepoStateListener remoteRepoListener);

    void removeRemoteRepoStateListener();

    void getRecord(@NonNull String name, @NonNull RecordGetter recordGetter);

    interface RecordGetter {
        void onRecordGot(FileInputStream record);

        void onRecordNotFound();
    }
}
