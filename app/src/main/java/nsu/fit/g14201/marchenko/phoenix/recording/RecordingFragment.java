package nsu.fit.g14201.marchenko.phoenix.recording;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.OnClick;
import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.recording.lowlevelrecording.CameraGLView;
import nsu.fit.g14201.marchenko.phoenix.ui.BaseFragment;
import nsu.fit.g14201.marchenko.phoenix.ui.dialogs.CorrigibleErrorDialog;
import nsu.fit.g14201.marchenko.phoenix.ui.dialogs.FatalErrorDialog;
import nsu.fit.g14201.marchenko.phoenix.ui.dialogs.IncorrigibleErrorDialog;

public class RecordingFragment extends BaseFragment
        implements RecordingContract.View {
    private RecordingContract.Presenter presenter;
    private Button recordingButton;

    public static RecordingFragment newInstance() {
        return new RecordingFragment();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recordingButton = view.findViewById(R.id.record_video_button);
    }

    @Override
    public void onStart() {
        super.onStart();
        CameraGLView cameraView = getView().findViewById(R.id.texture);
        cameraView.setVideoSize(1280, 720); // Entry point for video size parameters
        presenter.setOutputForVideo(cameraView);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.doOnResumeActions();
    }

    @Override
    public void onPause() {
        presenter.doOnPauseActions();
        super.onPause();
    }

    @OnClick(R.id.record_video_button)
    void onChangeRecordingStatus() {
        presenter.changeRecordingState();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_recording;
    }

    @Override
    public void setPresenter(RecordingContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onRecordingStarted() {
        recordingButton.setText(R.string.stop_recording);
    }

    @Override
    public void onRecordingFinished(String path) {
        recordingButton.setText(R.string.start_recording);
        showToast(String.format("%s: %s", getString(R.string.video_saved), path));
    }

    @Override
    public void showCorrigibleErrorDialog(String errorMessage) {
        CorrigibleErrorDialog.newInstance(null, errorMessage)
                .show(getChildFragmentManager(), null);
    }

    @Override
    public void showIncorrigibleErrorDialog(String errorMessage) {
        IncorrigibleErrorDialog.newInstance(null, errorMessage)
                .show(getChildFragmentManager(), null); //FIXME NOW multiline in resources
    }

    @Override
    public void showFatalErrorDialog(String errorMessage) {
        FatalErrorDialog.newInstance(null, errorMessage)
                .show(getChildFragmentManager(), null);
    }
}
