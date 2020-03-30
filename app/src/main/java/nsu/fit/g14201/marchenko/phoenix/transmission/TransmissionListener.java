package nsu.fit.g14201.marchenko.phoenix.transmission;


import androidx.annotation.NonNull;

public interface TransmissionListener {
    void onUnableToContinueTransmission(@NonNull TransmissionProblem problem);

    void onTransmissionFinished();
}
