package nsu.fit.g14201.marchenko.phoenix.context;


import nsu.fit.g14201.marchenko.phoenix.cloud.CloudAPI;
import nsu.fit.g14201.marchenko.phoenix.cloud.GoogleDriveAPI;
import nsu.fit.g14201.marchenko.phoenix.connection.SignInException;

public class Context {
    private CloudAPI cloudAPI;

    public Context(CloudAPI cloudAPI) {
        this.cloudAPI = cloudAPI;
    }

    public CloudAPI getCloudAPI() {
        return cloudAPI;
    }

    public static Context createContext(android.content.Context context) throws SignInException {
        return new Context(new GoogleDriveAPI(context));
    }
}
