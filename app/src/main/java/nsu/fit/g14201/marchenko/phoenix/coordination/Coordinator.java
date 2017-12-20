package nsu.fit.g14201.marchenko.phoenix.coordination;


import android.os.Parcelable;

public interface Coordinator extends Parcelable {
    public enum View {
        REGISTRATION
    }

    Class getNextClass(View view);
}
