package nsu.fit.g14201.marchenko.phoenix.transmission;


public class TransmissionDetailedProblem extends TransmissionProblem {
    private final String details;

    public TransmissionDetailedProblem(@TransmissionProblemType int problem, String details) {
        super(problem);
        this.details = details;
    }

    public String getMessage() {
        switch (problem) {
            case FAILED_TO_CREATE_VIDEO_FOLDER:
                return details;
            default: return null;
        }
    }
}
