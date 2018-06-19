package nsu.fit.g14201.marchenko.phoenix.sync;

import android.util.Log;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.model.record.Record;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RemoteReposControllerProviding;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage.LocalStorage;

public class SyncModule {
    private LocalStorage localStorage;
    private RemoteReposControllerProviding remoteReposController;

    public  SyncModule(LocalStorage localStorage, RemoteReposControllerProviding remoteReposController) {
        this.localStorage = localStorage;
        this.remoteReposController = remoteReposController;
    }

    public Observable<Record> getRecords() {
        List<Record> localRecords = localStorage.getRecords();

        Disposable disposable = remoteReposController.getRecords()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .toList()
                .observeOn(Schedulers.io())
                .map(recordList -> {
                    Record[] records = recordList.toArray(new Record[recordList.size()]);
                    return records;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        records -> {
                            for (Record record : records) {
                                Log.d(App.getTag2(), record.getTitle());
                            }
                        },
                        error -> {}
                );

        return Observable.create(emitter -> {
            for (Record record : localRecords) {
                emitter.onNext(record);
            }
            emitter.onComplete();
        });
    }
}
