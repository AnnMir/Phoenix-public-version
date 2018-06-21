package nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.Single;
import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.model.VideoTitleHandlerProviding;
import nsu.fit.g14201.marchenko.phoenix.model.record.Record;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRepositoryException;

public class PrivateExternalStorage implements LocalStorage {
    private final File path;
    private final VideoTitleHandlerProviding videoTitleHandler;
    private LocalStorageListener listener;

    private final Object gettingRecordSync = new Object();

    public PrivateExternalStorage(@NonNull Context context,
                                  @NonNull VideoTitleHandlerProviding videoTitleHandler) {
        path = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        this.videoTitleHandler = videoTitleHandler;
    }

    @Override
    public void createVideoRepository(@NonNull String name) {
        File directory = new File(path, name);
        if (!directory.mkdirs()) {
            listener.onFailedToCreateRepository();
        }

        listener.onRepositoryCreated(directory);
    }

    @Override
    public Single<FileInputStream> getRecord(@NonNull String name) {
        synchronized (gettingRecordSync) {
            return Single.create(emitter -> {
                try {
                    Log.e(App.getTag(), "getRecord " + Thread.currentThread().getName());
                    emitter.onSuccess(new FileInputStream(path + File.separator + name));
                } catch (FileNotFoundException e) {
                    emitter.onError(new RecordRepositoryException(
                            RecordRepositoryException.RECORD_NOT_FOUND
                    ));
                }
            });
        }
    }

    @Override
    public String getPath() {
        return path.getAbsolutePath() + File.separator;
    }

//    @Override
//    public Observable<Record> getRecords() {
//        return Observable.create(emitter -> {
//            File[] videoTitles = LocalStorageUtils.getVideoTitles(path, videoTitleHandler);
//            for (File file : videoTitles) {
//                if (file.isFile() && LocalStorageUtils.isVideo(file, videoTitleHandler)) {
//                    emitter.onNext(new Record(file));
//                    continue;
//                }
//                if (file.isDirectory()) {
//                    if (LocalStorageUtils.isFragmentedVideo(file, videoTitleHandler)) {
//                        emitter.onNext(new FragmentedRecord(file));
//                    }
//                }
//            }
//            emitter.onComplete();
//        });
//    }

    @Override
    public Set<Record> getRecords() {
        File[] videoTitles = LocalStorageUtils.getVideoTitles(path, videoTitleHandler);
        Set<Record> records = new HashSet<>(videoTitles.length);

        for (File file : videoTitles) {
            if (file.isFile()) {
                records.add(new Record(file));
                continue;
            }
            if (file.isDirectory()) {
                if (LocalStorageUtils.isFragmentedVideo(file, videoTitleHandler)) {
                    records.add(new Record(file));
                }
            }
        }

        return records;
    }

    @Override
    public Observable<String> getFragmentTitles(@NonNull String videoTitle) {
        return Observable.create(emitter -> {
            File[] fragmentTitles = LocalStorageUtils.getFragmentTitles(
                    new File(path, videoTitle), videoTitleHandler);
            for (File fragmentTitle : fragmentTitles) {
                emitter.onNext(fragmentTitle.getName());
            }
            emitter.onComplete();
        });
    }

    public void setListener(@NonNull LocalStorageListener listener) {
        this.listener = listener;
    }
}
