package nsu.fit.g14201.marchenko.phoenix.context;


import nsu.fit.g14201.marchenko.phoenix.connection.SignInException;
import nsu.fit.g14201.marchenko.phoenix.model.VideoTitleHandler;
import nsu.fit.g14201.marchenko.phoenix.model.VideoTitleHandlerProviding;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RemoteReposControllerProviding;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.RemoteRepositoriesController;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.cloudservice.googledrive.GoogleDriveService;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage.LocalStorage;
import nsu.fit.g14201.marchenko.phoenix.recordrepository.localstorage.PrivateExternalStorage;

public class Context {
    private LocalStorage localStorage;
    private RemoteReposControllerProviding remoteRepositoriesController;
    private VideoTitleHandlerProviding videoTitleHandler;

    public Context(LocalStorage localStorage,
                   RemoteReposControllerProviding remoteRepositoriesController,
                   VideoTitleHandlerProviding videoTitleHandler) {
        this.localStorage = localStorage;
        this.remoteRepositoriesController = remoteRepositoriesController;
        this.videoTitleHandler = videoTitleHandler;
    }

    public LocalStorage getLocalStorage() {
        return localStorage;
    }

    public RemoteReposControllerProviding getRemoteRepositoriesController() {
        return remoteRepositoriesController;
    }

    public VideoTitleHandlerProviding getVideoTitleHandler() {
        return videoTitleHandler;
    }

    public static Context createContext(android.content.Context context) throws SignInException {
        VideoTitleHandlerProviding videoTitleHandler = new VideoTitleHandler(
                "[\\d]{2}-[\\d]{2}-[\\d]{4}_[\\d]{2}:[\\d]{2}:[\\d]{2}",
                "[\\d]*",
                ".mp4",
                "dd-MM-yyyy_HH:mm:ss"
        );
        PrivateExternalStorage localStorage = new PrivateExternalStorage(context, videoTitleHandler);
        RemoteReposControllerProviding remoteReposController =
                new RemoteRepositoriesController();
        Context newContext = new Context(localStorage, remoteReposController, videoTitleHandler);
        localStorage.setListener(remoteReposController);

        GoogleDriveService googleDriveService = new GoogleDriveService(context);
        remoteReposController.addCloudService(googleDriveService);
        remoteReposController.createAppFolderIfNotExists();

        return newContext;
    }
}
