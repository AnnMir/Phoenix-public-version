package nsu.fit.g14201.marchenko.phoenix.transmission;


import android.support.annotation.NonNull;

import nsu.fit.g14201.marchenko.phoenix.BasePresenter;
import nsu.fit.g14201.marchenko.phoenix.recording.VideoFragmentListener;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.VideoFragmentPath;

public interface TransmissionContract {
    interface Presenter extends BasePresenter, TransmissionListener {
        VideoFragmentListener prepareForNewTransmission(@NonNull VideoFragmentPath videoFragmentPath);

        void setTransmissionListener(TransmissionListener listener);

        void removeTransmissionListener();
    }
}
