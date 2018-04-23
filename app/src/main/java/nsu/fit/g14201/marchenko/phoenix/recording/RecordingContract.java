package nsu.fit.g14201.marchenko.phoenix.recording;


import nsu.fit.g14201.marchenko.phoenix.BasePresenter;
import nsu.fit.g14201.marchenko.phoenix.BaseView;
import nsu.fit.g14201.marchenko.phoenix.context.Contextual;
import nsu.fit.g14201.marchenko.phoenix.recording.gl.CameraGLView;

public interface RecordingContract {
    interface View extends BaseView<Presenter> {
        void onRecordingStarted();

        void onRecordingFinished(String path);

        void showCorrigibleErrorDialog(String errorMessage);

        void showIncorrigibleErrorDialog(String errorMessage);

        void showFatalErrorDialog(String errorMessage);
    }

    interface Presenter extends BasePresenter, Contextual {
        void setOutputForVideo(CameraGLView view);

        void changeRecordingState();

        void doOnResumeActions();

        void doOnPauseActions();
    }
}
