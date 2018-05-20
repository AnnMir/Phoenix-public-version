package nsu.fit.g14201.marchenko.phoenix.recordmanagement;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import java.util.Arrays;
import java.util.List;

import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.model.record.Record;
import nsu.fit.g14201.marchenko.phoenix.model.record.RecordDateComparator;
import nsu.fit.g14201.marchenko.phoenix.ui.BaseFragment;

public class RecordManagementFragment extends BaseFragment implements RecordManagementContract.View {
    private RecordManagementContract.Presenter presenter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ProgressBar spinner;
    private List<Record> recordsToShow = null;

    public static RecordManagementFragment newInstance() {
        return new RecordManagementFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.records_recycler_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(
                getActivity(), LinearLayoutManager.VERTICAL));

        if (recordsToShow == null) {
            spinner = view.findViewById(R.id.video_list_progress_bar);
            spinner.setVisibility(View.VISIBLE);
        } else {
            showData(recordsToShow);
            recordsToShow.clear();
            recordsToShow = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        spinner.setVisibility(View.VISIBLE);
    }

    @Override
    public void setPresenter(RecordManagementContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_record_management;
    }

    @Override
    public void setDataForVideoList(List<Record> records) {
        if (recyclerView != null) {
            showData(records);
        } else {
            recordsToShow = records;
        }
    }

    private void showData(List<Record> records) {
        Record[] recordArray = records.toArray(new Record[records.size()]);
        Arrays.sort(recordArray, new RecordDateComparator(false));
        RecyclerView.Adapter adapter = new RecordsViewAdapter(recordArray);
        recyclerView.setAdapter(adapter);
        spinner.setVisibility(View.GONE);
    }
}
