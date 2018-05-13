package nsu.fit.g14201.marchenko.phoenix.recordmanagement;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.ui.BaseFragment;

public class RecordManagementFragment extends BaseFragment implements RecordManagementContract.View {
    private RecordManagementContract.Presenter presenter;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;


    public static RecordManagementFragment newInstance() {
        return new RecordManagementFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        recyclerView = view.findViewById(R.id.records_recycler_view);
//        recyclerView.setHasFixedSize(true);
//
//        layoutManager = new LinearLayoutManager(getContext());
//        recyclerView.setLayoutManager(layoutManager);

    }

    @Override
    public void setPresenter(RecordManagementContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_record_management;
    }

//    class RecordsAdapter extends RecyclerView.Adapter<>
}
