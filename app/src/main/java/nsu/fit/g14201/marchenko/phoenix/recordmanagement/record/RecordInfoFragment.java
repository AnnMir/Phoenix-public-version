package nsu.fit.g14201.marchenko.phoenix.recordmanagement.record;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.ui.fragments.BaseFragment;

public class RecordInfoFragment extends BaseFragment implements RecordInfoContract.View {
    private RecordInfoContract.Presenter presenter;
    private TextView titleTextView;

    public static RecordInfoFragment newInstance() {
        return new RecordInfoFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        titleTextView = view.findViewById(R.id.record_title);
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
}
