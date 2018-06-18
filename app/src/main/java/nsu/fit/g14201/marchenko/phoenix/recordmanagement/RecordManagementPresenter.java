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
//                            view.configureVideoList(records);
                            Record[] demoRecords = new Record[5];
                            for (int i = 0; i < 5; i++) {
                                demoRecords[i] = new Record();
                            }
                            demoRecords[0].title = "05/05/2018 15:32:21";
                            demoRecords[1].title = "05/05/2018 16:03:56";
                            demoRecords[2].title = "Разговор в кофейне";
                            demoRecords[3].title = "14/05/2018 21:57:03";
                            demoRecords[4].title = "24/05/2018 12:25:05";

                            demoRecords[2].date = "10/05/2018 19:07:38";

                            demoRecords[1].fromCloud = true;
                            demoRecords[2].fromCloud = true;

                            view.configureVideoList(demoRecords);
                        },
                        error -> {}
                );
    }
}
