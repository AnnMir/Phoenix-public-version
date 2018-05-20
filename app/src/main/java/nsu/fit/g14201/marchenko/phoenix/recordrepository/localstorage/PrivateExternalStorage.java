package nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import io.reactivex.Observable;
import io.reactivex.Single;
import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.model.VideoTitleHandlerProviding;
import nsu.fit.g14201.marchenko.phoenix.model.record.FragmentedRecord;
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

    @Override
    public Observable<Record> getRecords() {
        return Observable.create(emitter -> {
            File[] videoTitles = LocalStorageUtils.getVideoTitles(path, videoTitleHandler);
            for (File file : videoTitles) {
                if (file.isFile() && LocalStorageUtils.isVideo(file, videoTitleHandler)) {
                    emitter.onNext(new Record(file));
                    continue;
                }
                if (file.isDirectory()) {
                    int fragmentsNum = LocalStorageUtils.isFragmentedVideo(file, videoTitleHandler);
                    if (fragmentsNum > 0) {
                        emitter.onNext(new FragmentedRecord(file, fragmentsNum));
                    }
                }
            }
            emitter.onComplete();
        });
    }

    public void setListener(@NonNull LocalStorageListener listener) {
        this.listener = listener;
    }
}
