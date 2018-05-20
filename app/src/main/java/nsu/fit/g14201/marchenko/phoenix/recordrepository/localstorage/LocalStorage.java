package nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage;

import io.reactivex.Observable;
import nsu.fit.g14201.marchenko.phoenix.model.record.Record;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRepository;

public interface LocalStorage extends RecordRepository {
    String getPath();

    Observable<Record> getRecords();
}
