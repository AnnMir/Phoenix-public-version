package nsu.fit.g14201.marchenko.phoenix.recordmanagement;

import android.support.annotation.NonNull;

import java.util.Arrays;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import nsu.fit.g14201.marchenko.phoenix.model.record.Record;
import nsu.fit.g14201.marchenko.phoenix.model.record.RecordDateComparator;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RemoteReposControllerProviding;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage.LocalStorage;

public class RecordManagementPresenter implements RecordManagementContract.Presenter {
    private final RecordManagementContract.View view;
    private LocalStorage localStorage;
    private RemoteReposControllerProviding remoteReposController;
    private Record[] records;
    private RecordManagementContract.RecordSelectionListener recordSelectionListener;

    public RecordManagementPresenter(@NonNull RecordManagementContract.View view,
                                     @NonNull LocalStorage localStorage,
                                     @NonNull RemoteReposControllerProviding remoteReposController) {
        this.view = view;
        this.localStorage = localStorage;
        this.remoteReposController = remoteReposController;
    }

    @Override
    public void setRecordSelectionListener(RecordManagementContract.RecordSelectionListener listener) {
        recordSelectionListener = listener;
    }

    @Override
    public void onRecordSelected(int position) {
        recordSelectionListener.onRecordSelected(records[position]);
    }

    @Override
    public void onViewDestroyed() {
        // TODO
    }

    @Override
    public void start() {
        Disposable disposable = localStorage.getRecords()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .toList()
                .observeOn(Schedulers.io())
                .map(recordList -> {
                    Record[] records = recordList.toArray(new Record[recordList.size()]);
                    Arrays.sort(records, new RecordDateComparator(false));
                    return records;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        records -> {
                            this.records = records;
                            view.configureVideoList(records);
                        },
                        error -> {}
                );
    }
}
