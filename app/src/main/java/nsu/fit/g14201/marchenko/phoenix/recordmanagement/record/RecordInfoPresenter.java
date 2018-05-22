package nsu.fit.g14201.marchenko.phoenix.recordmanagement.record;


import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.util.Arrays;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.model.record.Record;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RemoteReposControllerProviding;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage.LocalStorage;
import nsu.fit.g14201.marchenko.phoenix.videoprocessing.VideoJoiner;

public class RecordInfoPresenter implements RecordInfoContract.Presenter {
    private RecordInfoContract.View view;
    private final Record record;
    private final LocalStorage localStorage;
    private final RemoteReposControllerProviding remoteReposController;

    public RecordInfoPresenter(@NonNull RecordInfoContract.View view,
                               @NonNull Record record,
                               @NonNull LocalStorage localStorage,
                               @NonNull RemoteReposControllerProviding remoteReposController) {
        this.view = view;
        this.record = record;
        this.localStorage = localStorage;
        this.remoteReposController = remoteReposController;
    }

    @Override
    public void start() {
        view.showTitle(record.getTitle());
    }

    @Override
    public void assemble() {
        Log.e(App.getTag(), record.getTitle());
        Disposable disposable = localStorage.getFragmentTitles(record.getTitle())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .toList()
                .observeOn(Schedulers.io())
                .subscribe(
                        fragmentNames -> {
                            String[] fragmentNamesArray = fragmentNames.toArray(
                                    new String[fragmentNames.size()]);
                            Arrays.sort(fragmentNamesArray);
                            VideoJoiner.joinFragments(
                                    record.getPath(),
                                    fragmentNamesArray,
                                    new File(record.getPath() + ".mp4")
                            ); // FIXME: Architecture
                            // TODO NEXT: Удаление после сборки
                            Log.e(App.getTag(), "DONE");
                        },
                        error -> {
                            Log.e(App.getTag(), "ERROR");
                        }
                );
    }
}
