package nsu.fit.g14201.marchenko.phoenix.recordmanagement.record;


import android.support.annotation.NonNull;

import java.util.Set;

import nsu.fit.g14201.marchenko.phoenix.BasePresenter;
import nsu.fit.g14201.marchenko.phoenix.BaseView;

public class RecordInfoContract {
    interface View extends BaseView<Presenter> {
        void showTitle(@NonNull String title);

        void enterLoadingMode();

        void quitLoadingMode();

        void showNoInternetDialog();

        void showNoRecordInCloud();

        void showErrorDialog();

        void showError(String errorMessage);

        void showMissingFragmentsDownloaded();

        void showAssemblyCompletion();
    }

    interface Presenter extends BasePresenter {
        void assemble();

        void assembleWithoutCloudFragments(Set<String> fragmentNames);
    }
}
