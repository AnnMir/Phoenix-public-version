package nsu.fit.g14201.marchenko.phoenix.recordmanagement;

import nsu.fit.g14201.marchenko.phoenix.BasePresenter;
import nsu.fit.g14201.marchenko.phoenix.BaseView;
import nsu.fit.g14201.marchenko.phoenix.context.Contextual;

public interface RecordManagementContract {
    interface View extends BaseView<Presenter> {

    }

    interface Presenter extends BasePresenter, Contextual {
        void onViewDestroyed();
    }
}
