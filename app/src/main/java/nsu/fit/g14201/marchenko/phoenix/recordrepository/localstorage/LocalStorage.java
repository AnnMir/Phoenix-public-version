package nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage;

import android.support.annotation.NonNull;

import java.util.Set;

import io.reactivex.Observable;
import nsu.fit.g14201.marchenko.phoenix.model.record.Record;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRepository;

public interface LocalStorage extends RecordRepository {
    String getPath();

    Set<Record> getRecords();

    Observable<String> getFragmentTitles(@NonNull String videoTitle);

    void createVideoRepository(@NonNull String name);
}
