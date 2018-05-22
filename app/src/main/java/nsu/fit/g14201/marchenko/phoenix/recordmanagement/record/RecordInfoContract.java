package nsu.fit.g14201.marchenko.phoenix.recordmanagement.record;


import android.support.annotation.NonNull;

import nsu.fit.g14201.marchenko.phoenix.BasePresenter;
import nsu.fit.g14201.marchenko.phoenix.BaseView;

public class RecordInfoContract {
    interface View extends BaseView<Presenter> {
        void showTitle(@NonNull String title);
    }

    interface Presenter extends BasePresenter {

    }
}
