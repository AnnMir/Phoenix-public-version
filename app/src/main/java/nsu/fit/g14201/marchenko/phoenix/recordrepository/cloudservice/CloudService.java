package nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import nsu.fit.g14201.marchenko.phoenix.model.record.Record;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRepository;

public interface CloudService extends RecordRepository {
    String getName();

    Completable createAppFolderIfNotExists();

    Single<RecordFolder> createVideoRepository(@NonNull String name);

    Completable transmitFragment(@NonNull RecordFolder folder,
                                 @NonNull FileInputStream inputStream,
                                 @NonNull String name);

    Observable<Record> getRecords();

    Maybe<RecordFolder> getRecordFolder(@NonNull Record record);

    Observable<String> getFragments(@NonNull RecordFolder recordFolder);

    Completable downloadFragment(@NonNull RecordFolder recordFolder, @NonNull File file);
}
