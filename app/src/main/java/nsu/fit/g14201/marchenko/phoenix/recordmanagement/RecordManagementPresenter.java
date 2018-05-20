package nsu.fit.g14201.marchenko.phoenix.recordmanagement;

import android.support.annotation.NonNull;

import nsu.fit.g14201.marchenko.phoenix.context.Context;

public class RecordManagementPresenter implements RecordManagementContract.Presenter {
    private final RecordManagementContract.View view;
    private Context appContext;

    public RecordManagementPresenter(@NonNull RecordManagementContract.View view) {
        this.view = view;
    }

    @Override
    public void onViewDestroyed() {
        // TODO
    }

    @Override
    public void start() {
//        appContext.getRemoteRepositoriesController().getRecords();
    }

    @Override
    public void setContext(Context context) {
        appContext = context;
    }
}
