package nsu.fit.g14201.marchenko.phoenix.utils;

public class SDCardNotReadyException extends Throwable {
    private String code;

    SDCardNotReadyException(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + "\nError code: " + code;
    }
}
