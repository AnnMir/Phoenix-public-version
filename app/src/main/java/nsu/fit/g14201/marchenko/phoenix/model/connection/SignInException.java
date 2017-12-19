package nsu.fit.g14201.marchenko.phoenix.model.connection;


public class SignInException extends Exception {
    public SignInException(String message) {
        super(message);
    }

    public SignInException(String message, Throwable cause) {
        super(message, cause);
    }
}
