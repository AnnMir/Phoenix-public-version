package nsu.fit.g14201.marchenko.phoenix.recordmanagement;

import java.util.List;

import nsu.fit.g14201.marchenko.phoenix.BasePresenter;
import nsu.fit.g14201.marchenko.phoenix.BaseView;
import nsu.fit.g14201.marchenko.phoenix.model.record.Record;

public interface RecordManagementContract {
    interface View extends BaseView<Presenter> {
        void setDataForVideoList(List<Record> records);
    }

    interface Presenter extends BasePresenter {
        void onViewDestroyed();
    }
}
