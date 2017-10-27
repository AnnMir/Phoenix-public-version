package nsu.fit.g14201.marchenko.phoenix.utils;


import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public class ActivityUtils {
    public static void addFragmentToActivity(@NonNull FragmentManager fragmentManager,
                                             @NonNull Fragment fragment,
                                             @IdRes int containerViewId) {
        fragmentManager.beginTransaction()
                .add(containerViewId, fragment)
                .commit();
    }
}
