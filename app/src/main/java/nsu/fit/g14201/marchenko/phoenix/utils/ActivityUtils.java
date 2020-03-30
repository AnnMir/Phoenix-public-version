package nsu.fit.g14201.marchenko.phoenix.utils;


import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class ActivityUtils {
    private ActivityUtils() {
    }

    public static void addFragmentToActivity(@NonNull FragmentManager fragmentManager,
                                             @NonNull Fragment fragment,
                                             @IdRes int containerViewId,
                                             @Nullable String tag) {
        fragmentManager.beginTransaction()
                .add(containerViewId, fragment, tag)
                .commit();
    }
}
