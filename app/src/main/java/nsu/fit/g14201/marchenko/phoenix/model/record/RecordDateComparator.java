package nsu.fit.g14201.marchenko.phoenix.model.record;

import java.util.Comparator;

public class RecordDateComparator implements Comparator<Record> {
    private boolean ascending;

    public RecordDateComparator(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public int compare(Record a, Record b) {
        return (ascending)? a.getTitle().compareTo(b.getTitle())
                : b.getTitle().compareTo(a.getTitle());
    }
}
