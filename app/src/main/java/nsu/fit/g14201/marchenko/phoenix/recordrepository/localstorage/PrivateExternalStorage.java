package nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import io.reactivex.Single;
import nsu.fit.g14201.marchenko.phoenix.App;
import nsu.fit.g14201.marchenko.phoenix.model.Record;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRepositoryException;

public class PrivateExternalStorage implements LocalStorage {
    private final File path;
    private final Pattern directoryPattern;
    private LocalStorageListener listener;

    private final Object gettingRecordSync = new Object();

    public PrivateExternalStorage(@NonNull Context context, @NonNull String directoryPattern) {
        path = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        Log.e(App.getTag(), "PATTERN: " + directoryPattern);
        this.directoryPattern = Pattern.compile(directoryPattern);
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
    public void getRecords() {
        Log.e(App.getTag(), "PATH: " + path);
        List<Record> records = new ArrayList<>();

        File[] recordNames = path.listFiles(file -> directoryPattern.matcher(file.getName()).matches());
        for (File file : recordNames) {
            if (file.isDirectory()) {
                records.add(new Record(file));
                Log.d(App.getTag(), "RECORD : " + file.getName());
            }
        }
    }

    public void setListener(@NonNull LocalStorageListener listener) {
        this.listener = listener;
    }
}
