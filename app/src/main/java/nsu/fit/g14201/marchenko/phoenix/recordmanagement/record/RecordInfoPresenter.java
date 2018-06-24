package nsu.fit.g14201.marchenko.phoenix.recordmanagement.record;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.connection.InternetConnectionHandler;
import nsu.fit.g14201.marchenko.phoenix.model.record.Record;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RemoteReposControllerProviding;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage.LocalStorage;
import nsu.fit.g14201.marchenko.phoenix.videoprocessing.VideoJoiner;

public class RecordInfoPresenter implements RecordInfoContract.Presenter {
    private RecordInfoContract.View view;
    private final Record record;
    private final LocalStorage localStorage;
    private final RemoteReposControllerProviding remoteReposController;
    private final Context context;

    public RecordInfoPresenter(@NonNull RecordInfoContract.View view,
                               @NonNull Record record,
                               @NonNull LocalStorage localStorage,
                               @NonNull RemoteReposControllerProviding remoteReposController,
                               @NonNull Context context) {
        this.view = view;
        this.record = record;
        this.localStorage = localStorage;
        this.remoteReposController = remoteReposController;
        this.context = context;
    }

    @Override
    public void start() {
        view.showTitle(record.getDateTime());
    }

    @Override
    public void assemble() {
        if (!InternetConnectionHandler.isConnected(context)) {
            view.showNoInternetDialog();
        } else {
            performAssembling();
        }
    }

    @Override
    public void assembleWithoutInternet(Set<String> fragmentNames) {
        view.enterLoadingMode();
        Set<String> localFragmentNames;
        if (fragmentNames == null) {
            localFragmentNames = new HashSet<>();
            Disposable disposable = localStorage.getFragmentTitles(record.getTitle())
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(
                            localFragmentNames::add,
                            error -> {
                                error.printStackTrace();
                                Log.e(App.getTag(), error.getMessage());
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    view.showError(context.getString(R.string.unexpected_error));
                                });
                            },
                            () -> {
                                String[] fragmentNamesArray = localFragmentNames.toArray(
                                        new String[localFragmentNames.size()]);
                                Arrays.sort(fragmentNamesArray);
                                joinFragments(fragmentNamesArray);
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    view.quitLoadingMode();
                                });
                            });
        } else {
                localFragmentNames = fragmentNames;
                Disposable disposable = Completable.create(emitter -> {
                    String[] fragmentNamesArray = localFragmentNames.toArray(
                            new String[localFragmentNames.size()]);
                    Arrays.sort(fragmentNamesArray);
                    joinFragments(fragmentNamesArray);
                    emitter.onComplete();
                })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            () -> {
                                view.quitLoadingMode();
                            },
                            error -> {
                                error.printStackTrace();
                                Log.e(App.getTag(), error.getMessage());
                            }
                    );
        }
    }

    private void performAssembling() {
        view.enterLoadingMode();
        Set<String> localFragmentNames = new HashSet<>();
        Disposable disposable = localStorage.getFragmentTitles(record.getTitle())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                        localFragmentNames::add,
                        error -> {
                            error.getMessage();
                            Log.e(App.getTag(), error.getMessage());
                            new Handler(Looper.getMainLooper()).post(() -> {
                                view.showError(context.getString(R.string.unexpected_error));
                            });
                        },
                        () -> {
                            // Set of local file names is ready
                            Disposable disposable1 = remoteReposController.getRecordFolder(record)
                                .subscribe(
                                    recordFolder -> {

                                    },
                                    error ->{
                                        Log.e(App.getTag(), error.getMessage());
                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            view.showErrorDialog();
                                        });
                                    },
                                    () -> {
                                        // No corresponding record in cloud
                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            view.showNoRecordInCloud();
                                        });
                                        assembleWithoutInternet(localFragmentNames);
                                    });

//                            List<Record> missingFragments = new LinkedList<>();
//                            Disposable disposable1 = remoteReposController.getFragments(record)
//                                    .observeOn(Schedulers.io())
//                                    .subscribe(
//                                            remoteFragment -> {
////                                                if (!localFragmentNames.contains(remoteFragment)) {
////                                                    missingFragments.add(remoteFragment);
////                                                }
//                                            },
//                                            error -> {
//                                                Log.d(App.getTag2(), "Error!");
//                                                Log.d(App.getTag2(), error.getLocalizedMessage());
//                                            }
//                                    );
                        }
                );
//                .flatMapCompletable(fragmentNames -> {
//                    String[] fragmentNamesArray = fragmentNames.toArray(
//                            new String[fragmentNames.size()]);
//                    Arrays.sort(fragmentNamesArray);
//
//
//
//
//
//
////                    joinFragments(fragmentNamesArray);
//                    // TODO NEXT: Удаление после сборки
//                    return Completable.complete();
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(view::quitLoadingMode, error -> {
//                    Log.e(App.getTag(), "ERROR" + error.getLocalizedMessage());
//                    view.quitLoadingMode();
//                });
    }

    private void joinFragments(String[] fragmentNamesArray) throws IOException {
        VideoJoiner.joinFragments(
                record.getPath(),
                fragmentNamesArray,
                new File(record.getPath() + ".mp4")
        ); // FIXME: Architecture
    }
}
