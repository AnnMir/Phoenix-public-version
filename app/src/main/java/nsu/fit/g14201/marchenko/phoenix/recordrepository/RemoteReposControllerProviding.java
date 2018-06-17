package nsu.fit.g14201.marchenko.phoenix.recordrepository;


import android.support.annotation.NonNull;

import java.io.FileInputStream;

import io.reactivex.Completable;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice.CloudService;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage.LocalStorageListener;

public interface RemoteReposControllerProviding extends LocalStorageListener {
    void addCloudService(@NonNull CloudService cloudService);

    void createAppFolderIfNotExists();

    void createVideoRepository(@NonNull String repositoryName);

    void setRemoteRepoStateListener(@NonNull RecordRemoteRepoStateListener remoteRepoListener);

    void removeRemoteRepoStateListener();

    Completable transmitVideo(@NonNull FileInputStream inputStream, @NonNull String name);
}
