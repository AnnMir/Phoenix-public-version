package nsu.fit.g14201.marchenko.phoenix.recording;


import nsu.fit.g14201.marchenko.phoenix.BasePresenter;
import nsu.fit.g14201.marchenko.phoenix.BaseView;
import nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.CameraGLView;

public interface RecordingContract {
    interface View extends BaseView<Presenter> {
        void onRecordingStarted();

        void onRecordingFinished(String path);

        void showCorrigibleErrorDialog(String errorMessage);

        void showIncorrigibleErrorDialog(String errorMessage);

        void showFatalErrorDialog(String errorMessage);
    }

    interface Presenter extends BasePresenter {
        void setOutputForVideo(CameraGLView view);

        void changeRecordingState();

        void doOnResumeActions();

        void doOnPauseActions();
    }
}
