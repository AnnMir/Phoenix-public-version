package nsu.fit.g14201.marchenko.phoenix.coordination;


import android.os.Parcel;
import android.os.Parcelable;

import lombok.NoArgsConstructor;
import nsu.fit.g14201.marchenko.phoenix.ui.MainActivity;

@NoArgsConstructor
public class CoordinatorImpl implements Coordinator {
    public static final Parcelable.Creator<CoordinatorImpl> CREATOR
            = new Parcelable.Creator<CoordinatorImpl>() {
        public CoordinatorImpl createFromParcel(Parcel in) {
            return new CoordinatorImpl(in);
        }

        public CoordinatorImpl[] newArray(int size) {
            return new CoordinatorImpl[size];
        }
    };

    private CoordinatorImpl(Parcel in) {
    }

    @Override
    public Class getNextClass(View view) {
        switch (view) {
            case REGISTRATION:
                return MainActivity.class;
            default:
                return null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }
}
