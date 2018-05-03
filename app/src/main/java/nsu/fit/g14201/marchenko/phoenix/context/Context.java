package nsu.fit.g14201.marchenko.phoenix.context;


import nsu.fit.g14201.marchenko.phoenix.cloud.CloudAPI;
import nsu.fit.g14201.marchenko.phoenix.cloud.googledrive.GoogleDriveAPI;
import nsu.fit.g14201.marchenko.phoenix.connection.SignInException;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRepositoriesController;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice.GoogleDriveService;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage.LocalStorage;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage.PrivateExternalStorage;

public class Context {
    private CloudAPI cloudAPI;
    private RecordRepositoriesController recordRepositoriesController;

    public Context(CloudAPI cloudAPI, LocalStorage localStorage) {
        this.cloudAPI = cloudAPI;
        recordRepositoriesController = new RecordRepositoriesController(localStorage);
        cloudAPI.setListener(recordRepositoriesController);
    }

    public RecordRepositoriesController getRecordRepositoriesController() {
        return recordRepositoriesController;
    }

    public static Context createContext(android.content.Context context) throws SignInException {
        GoogleDriveAPI googleDriveAPI = new GoogleDriveAPI(context);
        Context newContext = new Context(googleDriveAPI, new PrivateExternalStorage(context));
        newContext.recordRepositoriesController.addCloudService(new GoogleDriveService(googleDriveAPI));

        return newContext;
    }
}
