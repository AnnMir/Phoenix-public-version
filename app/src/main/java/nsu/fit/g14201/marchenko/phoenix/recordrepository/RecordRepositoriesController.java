package nsu.fit.g14201.marchenko.phoenix.recordrepository;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.network.cloud.RecordFolder;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice.CloudService;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage.LocalStorage;

public class RecordRepositoriesController implements RecordReposControllerProviding {
    private LocalStorage localStorage;
    private List<CloudService> cloudServices;
    private Map<CloudService, RecordFolder> recordFolders;

    private RecordRemoteRepoStateListener remoteRepoListener;
    private RecordLocalRepoStateListener localRepoListener; // TODO local repositories listener

    private RecordGetter recordGetter = null;

    public RecordRepositoriesController(LocalStorage localStorage) {
        this.localStorage = localStorage;
        cloudServices = new ArrayList<>();
        recordFolders = new HashMap<>();
    }

    @Override
    public void addCloudService(@NonNull CloudService cloudService) {
        cloudServices.add(cloudService);
    }

    @Override
    public void createVideoRepositoryLocally(@NonNull String repositoryName) {
        localStorage.createVideoRepository(repositoryName);
    }

    @Override
    public void createVideoRepositoryRemotely(@NonNull String repositoryName) {
        for (CloudService cloudService : cloudServices) {
            cloudService.createVideoRepository(repositoryName);
        }
    }

    @Override
    public String getLocalStoragePath() {
        return localStorage.getPath();
    }

    @Override
    public void setLocalRepoStateListener(@NonNull RecordLocalRepoStateListener localRepoListener) {
        this.localRepoListener = localRepoListener;
    }

    @Override
    public void removeLocalRepoStateListener() { // TODO: Use
        localRepoListener = null;
    }

    @Override
    public void setRemoteRepoStateListener(@NonNull RecordRemoteRepoStateListener remoteRepoListener) {
        this.remoteRepoListener = remoteRepoListener;
    }

    @Override
    public void removeRemoteRepoStateListener() { // TODO: Use
        remoteRepoListener = null;
    }

    @Override
    public void getRecord(@NonNull String name, @NonNull RecordGetter recordGetter) {
        localStorage.getRecord(name);
        this.recordGetter = recordGetter;
    }

    // LocalStorageListener

    @Override
    public void onRepositoryCreated(File repository) {
        Log.d(App.getTag(), "Video repository created locally: " + repository.getName());
    }

    @Override
    public void onFailedToCreateRepository() {
        // TODO WAITING FOR LOCAL REPO STATE LISTENER
    }

    @Override
    public void onRecordGot(FileInputStream record) {
        if (recordGetter != null) {
            recordGetter.onRecordGot(record);
            recordGetter = null;
        }
    }

    @Override
    public void onRecordNotFound() {
        if (recordGetter != null) {
            recordGetter.onRecordNotFound();
            recordGetter = null;
        }
    }

    // CloudServiceListener

    @Override
    public void onVideoRepositoryCreated(@NonNull CloudService cloudService,
                                         @NonNull RecordFolder repository) {
        Log.d(App.getTag(), "Video repository created on " + cloudService.getName());
        recordFolders.put(cloudService, repository);
    }

    @Override
    public void onFailedToCreateVideoRepository(@NonNull CloudService cloudService, Exception exception) {
        remoteRepoListener.onFailedToCreateVideoRepository(exception, cloudService.getName());
    }
}
