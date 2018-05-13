package nsu.fit.g14201.marchenko.phoenix.recordmanagement;

import nsu.fit.g14201.marchenko.phoenix.context.Context;

public class RecordManagementPresenter implements RecordManagementContract.Presenter {
    private final RecordManagementContract.View view;
    private Context appContext;

    public RecordManagementPresenter(RecordManagementContract.View view) {
        this.view = view;
    }

    @Override
    public void onViewDestroyed() {
        // TODO
    }

    @Override
    public void start() {
        appContext.getRecordRepositoriesController().getRecords();
    }

    @Override
    public void setContext(Context context) {
        appContext = context;
    }
}
