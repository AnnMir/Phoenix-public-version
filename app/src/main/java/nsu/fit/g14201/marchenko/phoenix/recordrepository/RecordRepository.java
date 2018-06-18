package nsu.fit.g14201.marchenko.phoenix.recordrepository;

import android.support.annotation.NonNull;

import java.io.FileInputStream;

import io.reactivex.Single;


public interface RecordRepository {
    Single<FileInputStream> getRecord(@NonNull String name);
}
