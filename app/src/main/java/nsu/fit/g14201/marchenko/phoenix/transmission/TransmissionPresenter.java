package nsu.fit.g14201.marchenko.phoenix.transmission;


import android.support.annotation.NonNull;

import nsu.fit.g14201.marchenko.phoenix.context.Context;
import nsu.fit.g14201.marchenko.phoenix.model.VideoFragmentPath;
import nsu.fit.g14201.marchenko.phoenix.recording.VideoFragmentListener;

public class TransmissionPresenter implements TransmissionContract.Presenter, TransmissionListener {
    private PeriodicRecordRemoteTransmitter transmitter;
    private TransmissionListener listener;
    private Context appContext;

    public TransmissionPresenter(Context appContext) {
        this.appContext = appContext;
    }

    @Override
    public void start() {
        transmitter.createVideoRepositories();
    }

    @Override
    public VideoFragmentListener prepareForNewTransmission(@NonNull VideoFragmentPath videoFragmentPath) {
        transmitter = new PeriodicRecordRemoteTransmitter(appContext.getLocalStorage(),
                appContext.getRemoteRepositoriesController(),
                videoFragmentPath);
        transmitter.setTransmissionListener(this);

        return transmitter;
    }

    @Override
    public void setTransmissionListener(TransmissionListener listener) {
        this.listener = listener;
    }

    @Override
    public void removeTransmissionListener() { // TODO: Use
        listener = null;
    }

    // TransmissionListener

    @Override
    public void onUnableToContinueTransmission(@NonNull TransmissionProblem problem) {
        transmitter.stop();
        transmitter.removeTransmissionListener();
        listener.onUnableToContinueTransmission(problem);
    }

    @Override
    public void onTransmissionFinished() {
        transmitter.stop();
        transmitter.removeTransmissionListener();
        listener.onTransmissionFinished();
    }
}

// TODO IMPORTANT: realize listeners' removal everywhere
