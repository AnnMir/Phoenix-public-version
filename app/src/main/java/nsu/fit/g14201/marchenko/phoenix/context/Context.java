package nsu.fit.g14201.marchenko.phoenix.context;


import nsu.fit.g14201.marchenko.phoenix.connection.SignInException;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RemoteReposControllerProviding;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RemoteRepositoriesController;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice.googledrive.GoogleDriveService;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage.LocalStorage;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage.PrivateExternalStorage;

public class Context {
    private LocalStorage localStorage;
    private RemoteReposControllerProviding remoteRepositoriesController;
    private String directoryPattern;

    public Context(LocalStorage localStorage,
                   RemoteReposControllerProviding remoteRepositoriesController,
                   String directoryPattern) {
        this.localStorage = localStorage;
        this.remoteRepositoriesController = remoteRepositoriesController;
        this.directoryPattern = directoryPattern;
    }

    public LocalStorage getLocalStorage() {
        return localStorage;
    }

    public RemoteReposControllerProviding getRemoteRepositoriesController() {
        return remoteRepositoriesController;
    }

    public String getDirectoryPattern() {
        return directoryPattern;
    }

    public static Context createContext(android.content.Context context) throws SignInException {
        PrivateExternalStorage localStorage = new PrivateExternalStorage(
                context,
                "[\\d]{2}-[\\d]{2}-[\\d]{4}_[\\d]{2}:[\\d]{2}:[\\d]{2}"
        );
        RemoteReposControllerProviding recordRepositoriesController =
                new RemoteRepositoriesController();
        Context newContext = new Context(localStorage,
                recordRepositoriesController,
                "dd-MM-yyyy_HH:mm:ss");
        localStorage.setListener(recordRepositoriesController);

        GoogleDriveService googleDriveService = new GoogleDriveService(context);
        googleDriveService.createAppFolderIfNotExists();

        recordRepositoriesController.addCloudService(googleDriveService);
        googleDriveService.setListener(recordRepositoriesController);

        return newContext;
    }
}
