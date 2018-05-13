package nsu.fit.g14201.marchenko.phoenix.recordrepository;


import android.support.annotation.NonNull;

import java.io.FileInputStream;

import io.reactivex.Completable;
import io.reactivex.Single;
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

    Single<FileInputStream> getRecord(@NonNull String name);

    void getRecords();

    Completable transmitVideo(@NonNull FileInputStream inputStream, @NonNull String name);
}
