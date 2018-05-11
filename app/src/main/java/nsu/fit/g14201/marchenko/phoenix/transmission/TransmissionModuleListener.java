package nsu.fit.g14201.marchenko.phoenix.transmission;


import android.support.annotation.NonNull;

public interface TransmissionModuleListener {
    void onUnableToContinueTransmission(@NonNull TransmissionProblem problem);

    void onTransmissionFinished();
}

// FIXME: Unite with TransmissionListener?
