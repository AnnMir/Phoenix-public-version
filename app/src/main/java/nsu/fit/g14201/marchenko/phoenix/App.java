package nsu.fit.g14201.marchenko.phoenix;


import android.app.Application;

public class App extends Application {
    private static final String TAG = "safari";
    public static final String APP_NAME = "Phoenix";
    private static final String EXTRA_COORDINATOR =
            "nsu.fit.g14201.marchenko.phoenix.registration.COORDINATOR";

    public static String getTag() {
        return TAG;
    }
    public static String getExtraCoordinator() {
        return EXTRA_COORDINATOR;
    }
}
