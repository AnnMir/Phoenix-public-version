package nsu.fit.g14201.marchenko.phoenix.recordmanagement;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import nsu.fit.g14201.marchenko.phoenix.model.record.Record;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RemoteReposControllerProviding;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage.LocalStorage;

public class RecordManagementPresenter implements RecordManagementContract.Presenter {
    private final RecordManagementContract.View view;
    private LocalStorage localStorage;
    private RemoteReposControllerProviding remoteReposController;

    public RecordManagementPresenter(@NonNull RecordManagementContract.View view,
                                     @NonNull LocalStorage localStorage,
                                     @NonNull RemoteReposControllerProviding remoteReposController) {
        this.view = view;
        this.localStorage = localStorage;
        this.remoteReposController = remoteReposController;
    }

    @Override
    public void onViewDestroyed() {
        // TODO
    }

    @Override
    public void start() {
        List<Record> records = new ArrayList<>();
        Disposable disposable = localStorage.getRecords()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        records::add,
                        error -> {},
                        () -> {
                            view.setDataForVideoList(records);
                        }
                );
    }
}
