package nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice;

import android.support.annotation.NonNull;

import java.io.FileInputStream;

import io.reactivex.Completable;
import io.reactivex.Single;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRepository;

public interface CloudService extends RecordRepository {
    String getName();

    Completable createAppFolderIfNotExists();

    Single<RecordFolder> createVideoRepository(@NonNull String name);

    Completable transmitFragment(@NonNull RecordFolder folder,
                                 @NonNull FileInputStream inputStream,
                                 @NonNull String name);
}
