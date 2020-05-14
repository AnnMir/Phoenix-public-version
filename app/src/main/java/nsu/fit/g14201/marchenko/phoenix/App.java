package nsu.fit.g14201.marchenko.phoenix;


import android.app.Application;

public class App extends Application {
    private static final String TAG = "safari";
    private static final String APP_NAME = "Phoenix";
    private static final String EXTRA_COORDINATOR =
            "nsu.fit.g14201.marchenko.phoenix.registration.COORDINATOR";
    private static String appFolderId;

    public static String getTag() {
        return TAG;
    }
    public static String getTag2() {
        return "safari5";
    }
    public static String getAppName() {
        return APP_NAME;
    }
    public static String getExtraCoordinator() {
        return EXTRA_COORDINATOR;
    }
    public static void setAppFolderId(String id){ appFolderId = id; }
    public static String getAppFolderId(){ return appFolderId; }
}
