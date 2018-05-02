package nsu.fit.g14201.marchenko.phoenix.context;


import nsu.fit.g14201.marchenko.phoenix.cloud.CloudAPI;
import nsu.fit.g14201.marchenko.phoenix.cloud.googledrive.GoogleDriveAPI;
import nsu.fit.g14201.marchenko.phoenix.connection.SignInException;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RecordRepositoriesController;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice.GoogleDriveService;

public class Context {
    private CloudAPI cloudAPI;
    private RecordRepositoriesController recordRepositoriesController;

    public Context(CloudAPI cloudAPI) {
        this.cloudAPI = cloudAPI;
        recordRepositoriesController = new RecordRepositoriesController();
        cloudAPI.setListener(recordRepositoriesController);
    }

    public RecordRepositoriesController getRecordRepositoriesController() {
        return recordRepositoriesController;
    }

    public static Context createContext(android.content.Context context) throws SignInException {
        GoogleDriveAPI googleDriveAPI = new GoogleDriveAPI(context);
        Context newContext = new Context(googleDriveAPI);
        newContext.recordRepositoriesController.addRepository(new GoogleDriveService(googleDriveAPI));

        return newContext;
    }
}
