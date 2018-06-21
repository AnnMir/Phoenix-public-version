package nsu.fit.g14201.marchenko.phoenix.recordmanagement;

import android.content.Context;
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
import nsu.fit.g14201.marchenko.phoenix.ui.fragments.BaseFragment;
import nsu.fit.g14201.marchenko.phoenix.utils.ItemClickSupport;

public class RecordManagementFragment extends BaseFragment implements RecordManagementContract.View {
    private RecordManagementContract.Presenter presenter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ProgressBar progressBar;
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
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof RecordManagementContract.RecordSelectionListener) {
            presenter.setRecordSelectionListener(
                    (RecordManagementContract.RecordSelectionListener) context);
        } else {
            throw new RuntimeException(context.toString() +
                    " must implement RecordSelectionListener");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.records_recycler_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getContext());

        progressBar = view.findViewById(R.id.video_list_progress_bar);
    }

    @Override
    public void onStart() {
        super.onStart();

        progressBar.setVisibility(View.VISIBLE);
        presenter.start();
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
        progressBar.setVisibility(View.GONE);
    }
}
