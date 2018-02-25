package nsu.fit.g14201.marchenko.phoenix.recording;


import nsu.fit.g14201.marchenko.phoenix.BasePresenter;
import nsu.fit.g14201.marchenko.phoenix.BaseView;

public interface RecordingContract {
    interface View extends BaseView<Presenter> {
        void showCorrigibleErrorDialog(String errorMessage);

        void showIncorrigibleErrorDialog(String errorMessage);

        void showFatalErrorDialog(String errorMessage);
    }

    interface Presenter extends BasePresenter {

    }
}
