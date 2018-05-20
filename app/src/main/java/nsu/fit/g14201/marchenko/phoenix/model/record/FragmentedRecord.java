package nsu.fit.g14201.marchenko.phoenix.model.record;

import android.support.annotation.NonNull;

import java.io.File;

public class FragmentedRecord extends Record {
    private int fragmentsNum;

    public FragmentedRecord(@NonNull File path, int fragmentsNum) {
        super(path);
        this.fragmentsNum = fragmentsNum;
    }

    public int getFragmentsNum() {
        return fragmentsNum;
    }
}
