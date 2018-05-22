package nsu.fit.g14201.marchenko.phoenix.recordmanagement.record;


import android.support.annotation.NonNull;

import nsu.fit.g14201.marchenko.phoenix.model.record.Record;

public class RecordInfoPresenter implements RecordInfoContract.Presenter {
    private RecordInfoContract.View view;
    private Record record;

    public RecordInfoPresenter(@NonNull RecordInfoContract.View view, @NonNull Record record) {
        this.view = view;
        this.record = record;
    }

    @Override
    public void start() {
        view.showTitle(record.getTitle());
    }
}
