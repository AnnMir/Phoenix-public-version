package nsu.fit.g14201.marchenko.phoenix.recording;


import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.ui.BaseFragment;
import nsu.fit.g14201.marchenko.phoenix.ui.dialogs.CorrigibleErrorDialog;
import nsu.fit.g14201.marchenko.phoenix.ui.dialogs.FatalErrorDialog;
import nsu.fit.g14201.marchenko.phoenix.ui.dialogs.IncorrigibleErrorDialog;

public class RecordingFragment extends BaseFragment
        implements RecordingContract.View {
    private static final String FRAGMENT_DIALOG = "dialog"; //FIXME NOW tag
    private RecordingContract.Presenter presenter;

    public static RecordingFragment newInstance() {
        return new RecordingFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.setOutputForVideo(getView().findViewById(R.id.texture));
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

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_recording;
    }

    @Override
    public void setPresenter(RecordingContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showCorrigibleErrorDialog(String errorMessage) {
        CorrigibleErrorDialog.newInstance(null, errorMessage)
                .show(getChildFragmentManager(), FRAGMENT_DIALOG);
    }

    @Override
    public void showIncorrigibleErrorDialog(String errorMessage) {
        IncorrigibleErrorDialog.newInstance(null, errorMessage)
                .show(getChildFragmentManager(), FRAGMENT_DIALOG); //FIXME NOW multiline in resources
    }

    @Override
    public void showFatalErrorDialog(String errorMessage) {
        FatalErrorDialog.newInstance(null, errorMessage)
                .show(getChildFragmentManager(), FRAGMENT_DIALOG);
    }
}
