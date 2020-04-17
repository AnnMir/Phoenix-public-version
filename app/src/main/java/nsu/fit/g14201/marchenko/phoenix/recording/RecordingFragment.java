package nsu.fit.g14201.marchenko.phoenix.recording;


import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.OnClick;
import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.notifications.NotificationsPresenter;
import nsu.fit.g14201.marchenko.phoenix.recording.gl.CameraGLView;
import nsu.fit.g14201.marchenko.phoenix.ui.activities.MainActivity;
import nsu.fit.g14201.marchenko.phoenix.ui.dialogs.CorrigibleErrorDialog;
import nsu.fit.g14201.marchenko.phoenix.ui.dialogs.FatalErrorDialog;
import nsu.fit.g14201.marchenko.phoenix.ui.dialogs.IncorrigibleErrorDialog;
import nsu.fit.g14201.marchenko.phoenix.ui.fragments.BaseFragment;

public class RecordingFragment extends BaseFragment implements RecordingContract.View {
    private RecordingContract.Presenter presenter;
    private Button recordingButton;
    private FloatingActionButton call;
    private NotificationsPresenter notificationsPresenter;

    public static RecordingFragment newInstance() {
        return new RecordingFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recordingButton = view.findViewById(R.id.record_video_button);
        call = view.findViewById(R.id.call);

    }

    @Override
    public void onStart() {
        super.onStart();
        CameraGLView cameraView = getView().findViewById(R.id.texture);
//        cameraView.setVideoSize(1280, 720); // Entry point for video size parameters
        presenter.setOutputForVideo(cameraView);
        notificationsPresenter = new NotificationsPresenter(getView().getContext());
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

    @OnClick(R.id.call)
    public void call(View v){
        notificationsPresenter.call(v);
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
