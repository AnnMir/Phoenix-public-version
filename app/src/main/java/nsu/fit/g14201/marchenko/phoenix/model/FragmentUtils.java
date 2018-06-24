package nsu.fit.g14201.marchenko.phoenix.model;

public class FragmentUtils {
    private FragmentUtils() {}

    static public Integer getFragmentNumber(String fragmentPath) {
        int endIndex = fragmentPath.lastIndexOf(".");
        return Integer.parseInt(fragmentPath.substring(0, endIndex));
    }
}
