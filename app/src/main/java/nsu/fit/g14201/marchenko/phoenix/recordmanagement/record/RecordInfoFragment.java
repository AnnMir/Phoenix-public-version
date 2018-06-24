package nsu.fit.g14201.marchenko.phoenix.recordmanagement.record;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.OnClick;
import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.ui.fragments.BaseFragment;

public class RecordInfoFragment extends BaseFragment implements RecordInfoContract.View {
    private RecordInfoContract.Presenter presenter;
    private TextView titleTextView;
    private Button assembleButton;
    private ProgressBar progressBar;

    public static RecordInfoFragment newInstance() {
        return new RecordInfoFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titleTextView = view.findViewById(R.id.record_title);
        assembleButton = view.findViewById(R.id.assemble_button);
        progressBar = view.findViewById(R.id.record_info_progress_bar);

        presenter.start();
    }

    @Override
    public void setPresenter(RecordInfoContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_record_info;
    }

    @Override
    public void showTitle(@NonNull String title) {
        titleTextView.setText(title);
    }

    @Override
    public void enterLoadingMode() {
        assembleButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void quitLoadingMode() {
        assembleButton.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }

    @OnClick(R.id.assemble_button)
    void onAssembleClick() {
        presenter.assemble();
    }
}
