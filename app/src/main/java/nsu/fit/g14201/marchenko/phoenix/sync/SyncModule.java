package nsu.fit.g14201.marchenko.phoenix.sync;

import java.util.Arrays;
import java.util.Set;

import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import nsu.fit.g14201.marchenko.phoenix.model.record.Record;
import nsu.fit.g14201.marchenko.phoenix.model.record.RecordDateComparator;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RemoteReposControllerProviding;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage.LocalStorage;

public class SyncModule {
    private LocalStorage localStorage;
    private RemoteReposControllerProviding remoteReposController;

    public  SyncModule(LocalStorage localStorage, RemoteReposControllerProviding remoteReposController) {
        this.localStorage = localStorage;
        this.remoteReposController = remoteReposController;
    }

    public Single<Record[]> getRecords() {
        return Single.create(emitter -> {
            Set<Record> recordSet = localStorage.getRecords();

            Disposable disposable = remoteReposController.getRecords()
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(
                            record -> {
                                recordSet.add(record);
                            },
                            error -> {
                                emitter.onError(error);
                            },
                            () -> {
                                Record[] records = recordSet.toArray(new Record[recordSet.size()]);
                                Arrays.sort(records, new RecordDateComparator(false));
                                emitter.onSuccess(records);
                            }
                    );
        });
    }
}
