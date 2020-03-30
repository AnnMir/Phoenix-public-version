package nsu.fit.g14201.marchenko.phoenix.recordmanagement.record;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.NonNull;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.connection.InternetConnectionHandler;
import nsu.fit.g14201.marchenko.phoenix.model.FragmentUtils;
import nsu.fit.g14201.marchenko.phoenix.model.record.Record;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RemoteReposControllerProviding;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice.RecordFolder;
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
    public void assembleWithoutCloudFragments(Set<String> fragmentNames) {
        Set<String> localFragmentNames;
        if (fragmentNames == null) {
            view.enterLoadingMode();
            localFragmentNames = new HashSet<>();
            Disposable disposable = localStorage.getFragmentTitles(record.getTitle())
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(
                            localFragmentNames::add,
                            this::handleError,
                            () -> {
                                String[] fragmentNamesArray = localFragmentNames.toArray(
                                        new String[localFragmentNames.size()]);
                                Arrays.sort(fragmentNamesArray, (o1, o2) -> FragmentUtils
                                        .getFragmentNumber(o1)
                                        .compareTo(FragmentUtils.getFragmentNumber(o2)));
                                joinFragments(fragmentNamesArray);
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    view.quitLoadingMode();
                                    view.showAssemblyCompletion();
                                });
                            });
        } else {
                localFragmentNames = fragmentNames;
                Disposable disposable = Completable.create(emitter -> {
                    String[] fragmentNamesArray = localFragmentNames.toArray(
                            new String[localFragmentNames.size()]);
                    Arrays.sort(fragmentNamesArray, (o1, o2) -> FragmentUtils.getFragmentNumber(o1)
                            .compareTo(FragmentUtils.getFragmentNumber(o2)));
                    joinFragments(fragmentNamesArray);
                    emitter.onComplete();
                })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            () -> {
                                view.quitLoadingMode();
                                view.showAssemblyCompletion();
                            },
                            this::handleError
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
                        this::handleError,
                        () -> {
                            // Set of local file names is ready
                            Disposable disposable1 = remoteReposController.getRecordFolder(record)
                                .subscribe(
                                    recordFolder -> {
                                        assembleConsideringCloudFragments(recordFolder,
                                                localFragmentNames);
                                    },
                                        error -> {
                                            error.printStackTrace();
                                            Log.e(App.getTag(), error.getMessage());
                                            new Handler(Looper.getMainLooper()).post(() -> {
                                                view.quitLoadingMode();
                                                view.showErrorDialog();
                                            });
                                        },
                                    () -> {
                                        // No corresponding record in cloud
                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            view.showNoRecordInCloud();
                                        });
                                        assembleWithoutCloudFragments(localFragmentNames);
                                    });
                        }
                );
    }

    private void joinFragments(String[] fragmentNamesArray) throws IOException {
        VideoJoiner.joinFragments(
                record.getPath(),
                fragmentNamesArray,
                new File(record.getPath() + ".mp4")
        ); // FIXME: Architecture
    }

    private void assembleConsideringCloudFragments(@NonNull RecordFolder recordFolder,
                                                   @NonNull Set<String> localFragmentNames) {
        List<Completable> missingFragments = new ArrayList<>();
        Disposable disposable = remoteReposController.getFragments(recordFolder)
                .subscribe(
                        remoteFragmentName -> {
                            if (!localFragmentNames.contains(remoteFragmentName)) {
                                File fragment = new File(record.getPath(), remoteFragmentName);
                                missingFragments.add(remoteReposController.downloadFragment(
                                        recordFolder, fragment));
                            }
                        },
                        this::handleError,
                        () -> {
                            if (missingFragments.isEmpty()) {
                                assembleWithoutCloudFragments(localFragmentNames);
                            } else {
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    view.showLoadingMissingFragments();
                                });
                                AtomicInteger fragmentsToDownload = new AtomicInteger(
                                        missingFragments.size());
                                for (Completable missingFragmentCompletable : missingFragments) {
                                    Disposable disposable1 = missingFragmentCompletable.subscribe(
                                            () -> {
                                                int fragmentsLeft = fragmentsToDownload.decrementAndGet();
                                                if (fragmentsLeft == 0) {
                                                    assembleWithoutCloudFragments(null);
                                                }
                                            },
                                            this::handleError
                                    );
                                }
                            }
                        }
                );
    }

    private void handleError(Throwable error) {
        error.printStackTrace();
        Log.e(App.getTag(), error.getMessage());
        new Handler(Looper.getMainLooper()).post(() -> {
            view.quitLoadingMode();
            view.showError(context.getString(R.string.unexpected_error));
        });
    }
}
