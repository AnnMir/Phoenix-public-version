package nsu.fit.g14201.marchenko.phoenix.recordmanagement;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.model.record.Record;
import nsu.fit.g14201.marchenko.phoenix.ui.BaseFragment;
import nsu.fit.g14201.marchenko.phoenix.utils.ItemClickSupport;

public class RecordManagementFragment extends BaseFragment implements RecordManagementContract.View {
    private RecordManagementContract.Presenter presenter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ProgressBar spinner;
    private ItemClickSupport.OnItemClickListener onItemClickListener = new ItemClickSupport.OnItemClickListener() {
        @Override
        public void onItemClicked(RecyclerView recyclerView, int position, View v) {
            presenter.onRecordSelected(position);
        }
    };

    public static RecordManagementFragment newInstance() {
        return new RecordManagementFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.records_recycler_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getContext());

        if (recyclerView == null) {
            spinner = view.findViewById(R.id.video_list_progress_bar);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (recyclerView == null) {
            spinner.setVisibility(View.VISIBLE);
        }
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
    public void configureVideoList(Record[] records) {
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(
                getActivity(), LinearLayoutManager.VERTICAL));
        ItemClickSupport itemClickSupport = ItemClickSupport.addTo(recyclerView);
        itemClickSupport.setOnItemClickListener(onItemClickListener);

        RecyclerView.Adapter adapter = new RecordsViewAdapter(records);
        recyclerView.setAdapter(adapter);
        if (spinner != null) {
            spinner.setVisibility(View.GONE);
        }
    }
}
