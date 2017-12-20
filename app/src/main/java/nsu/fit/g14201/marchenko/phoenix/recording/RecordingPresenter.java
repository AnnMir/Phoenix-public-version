package nsu.fit.g14201.marchenko.phoenix.recording;


import android.content.Context;

import nsu.fit.g14201.marchenko.phoenix.model.connection.UserConnectionImpl;

public class RecordingPresenter implements RecordingContract.Presenter {
    private final RecordingContract.View recordingView;
    private final Context context;
    private UserConnectionImpl userConnection;

    public RecordingPresenter(Context applicationContext, RecordingContract.View recordingView) {
        context = applicationContext;
        this.recordingView = recordingView;
        recordingView.setPresenter(this);
    }

    @Override
    public void start() {

    }
}
