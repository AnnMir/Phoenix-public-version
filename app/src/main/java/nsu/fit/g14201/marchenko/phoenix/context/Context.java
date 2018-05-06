package nsu.fit.g14201.marchenko.phoenix.context;


import nsu.fit.g14201.marchenko.phoenix.network.cloud.CloudAPI;
import nsu.fit.g14201.marchenko.phoenix.network.cloud.googledrive.GoogleDriveAPI;
import nsu.fit.g14201.marchenko.phoenix.connection.SignInException;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRepositoriesController;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice.GoogleDriveService;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage.LocalStorage;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage.PrivateExternalStorage;

public class Context {
    private CloudAPI cloudAPI; // FIXME: Do we need it here?
    private RecordRepositoriesController recordRepositoriesController;

    public Context(CloudAPI cloudAPI, LocalStorage localStorage) {
        this.cloudAPI = cloudAPI;
        recordRepositoriesController = new RecordRepositoriesController(localStorage);
    }

    public RecordRepositoriesController getRecordRepositoriesController() {
        return recordRepositoriesController;
    }

    public static Context createContext(android.content.Context context) throws SignInException {
        GoogleDriveAPI googleDriveAPI = new GoogleDriveAPI(context);
        PrivateExternalStorage localStorage = new PrivateExternalStorage(context);
        Context newContext = new Context(googleDriveAPI, localStorage);
        localStorage.setListener(newContext.recordRepositoriesController);

        GoogleDriveService googleDriveService = new GoogleDriveService(googleDriveAPI);
        newContext.recordRepositoriesController.addCloudService(googleDriveService);
        googleDriveService.setListener(newContext.recordRepositoriesController);

        return newContext;
    }
}
