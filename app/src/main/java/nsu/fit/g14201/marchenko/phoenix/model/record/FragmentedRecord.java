package nsu.fit.g14201.marchenko.phoenix.model.record;



import java.io.File;

import androidx.annotation.NonNull;

public class FragmentedRecord extends Record {
    private String[] fragmentNames;

    public FragmentedRecord(@NonNull File path) {
        super(path);
    }

    public void setFragmentNames(@NonNull String[] fragmentNames) {
        this.fragmentNames = fragmentNames;
    }

    public String[] getFragmentNames() {
        return fragmentNames;
    }
}
