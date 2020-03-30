package nsu.fit.g14201.marchenko.phoenix.recordrepository;

import androidx.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.model.record.Record;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice.CloudService;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice.RecordFolder;

public class RemoteRepositoriesController implements RemoteReposControllerProviding {
    private List<CloudService> cloudServices;
    private Map<CloudService, RecordFolder> recordFolders;

    private RecordRemoteRepoStateListener remoteRepoListener;

    private final Scheduler scheduler;
    private final Object transmissionSync = new Object();
    private boolean appFolderCreated = false;
    private boolean videoRepositoryCreated = false;

    public RemoteRepositoriesController() {
        cloudServices = new ArrayList<>();
        recordFolders = new HashMap<>();
        scheduler = Schedulers.single();
    }

    @Override
    public void addCloudService(@NonNull CloudService cloudService) {
        cloudServices.add(cloudService); // FIXME: To disable cloud service
    }

    @Override
    public void createAppFolderIfNotExists() {
        /*
        final Disposable disposable = cloudServices.get(0).createAppFolderIfNotExists()
                .subscribeOn(scheduler)
                .observeOn(scheduler)
                .subscribe(() -> {
                    synchronized (transmissionSync) {
                        appFolderCreated = true;
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                    remoteRepoListener.onFailedToCreateAppRepository();
                });
         */
    }

    @Override
    public void createVideoRepository(@NonNull String repositoryName) {
        videoRepositoryCreated = false;
        for (CloudService cloudService : cloudServices) {
            final Disposable disposable = cloudService.createVideoRepository(repositoryName)
                    .subscribeOn(scheduler)
                    .observeOn(scheduler)
                    .subscribe(recordFolder -> {
                        recordFolders.put(cloudService, recordFolder);
                        synchronized (transmissionSync) {
                            videoRepositoryCreated = true;
                            transmissionSync.notifyAll();
                        }
                    }, throwable -> {
                        throwable.printStackTrace();
                        remoteRepoListener.onFailedToCreateVideoRepository(throwable,
                                cloudService.getName());
                    });
        }
    }

    @Override
    public Observable<Record> getRecords() {
        return cloudServices.get(0).getRecords();
    }

    @Override
    public Maybe<RecordFolder> getRecordFolder(@NonNull Record record) {
        return cloudServices.get(0).getRecordFolder(record);
    }

    @Override
    public Observable<String> getFragments(@NonNull RecordFolder recordFolder) {
        return cloudServices.get(0).getFragments(recordFolder);
    }

    @Override
    public Completable downloadFragment(@NonNull RecordFolder recordFolder, @NonNull File file) {
        return cloudServices.get(0).downloadFragment(recordFolder, file);
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
    public Completable transmitVideo(@NonNull FileInputStream inputStream, @NonNull String name) {
        synchronized (transmissionSync) { // FIXME: Move from here?
            while (!appFolderCreated || !videoRepositoryCreated) {
                try {
                    transmissionSync.wait();
                } catch (InterruptedException e) {}
            }
            CloudService currentCloudService = cloudServices.get(0);
            return currentCloudService.transmitFragment(
                    recordFolders.get(currentCloudService), inputStream, name
            );
        }
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
}
