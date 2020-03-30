package nsu.fit.g14201.marchenko.phoenix.recordmanagement;


import androidx.annotation.NonNull;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import nsu.fit.g14201.marchenko.phoenix.model.record.Record;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RemoteReposControllerProviding;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage.LocalStorage;
import nsu.fit.g14201.marchenko.phoenix.sync.SyncModule;

public class RecordManagementPresenter implements RecordManagementContract.Presenter {
    private final RecordManagementContract.View view;
    private LocalStorage localStorage;
    private RemoteReposControllerProviding remoteReposController;
    private Record[] records;
    private RecordManagementContract.RecordSelectionListener recordSelectionListener;
    private SyncModule syncModule;

    public RecordManagementPresenter(@NonNull RecordManagementContract.View view,
                                     @NonNull LocalStorage localStorage,
                                     @NonNull RemoteReposControllerProviding remoteReposController) {
        this.view = view;
        this.localStorage = localStorage;
        this.remoteReposController = remoteReposController;
        syncModule = new SyncModule(localStorage, remoteReposController);
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
        if (records != null) {
            view.configureVideoList(records);
            return;
        }

        Disposable disposable = syncModule.getRecords()
                .subscribeOn(Schedulers.io())
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
