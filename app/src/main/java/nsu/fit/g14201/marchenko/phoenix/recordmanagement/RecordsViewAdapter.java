package nsu.fit.g14201.marchenko.phoenix.recordmanagement;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import nsu.fit.g14201.marchenko.phoenix.R;
import nsu.fit.g14201.marchenko.phoenix.model.record.Record;

class RecordsViewAdapter extends RecyclerView.Adapter<RecordsViewAdapter.ViewHolder> {
    private Record[] records;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView videoTitle;
        TextView date;

        ViewHolder(View itemView) {
            super(itemView);

            this.itemView = itemView;
            videoTitle = itemView.findViewById(R.id.title);
            date = itemView.findViewById(R.id.date);
        }
    }

    RecordsViewAdapter(Record[] records) {
        this.records = records;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_list_element, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Record record = records[position];
        String userNameTitle = record.getUserNamedTitle();
        if (userNameTitle == null) {
            holder.videoTitle.setText(record.getDateTime());
            holder.date.setText(null);
        } else {
            holder.videoTitle.setText(userNameTitle);
            holder.date.setText(record.getDateTime());
        }

        if (record.isInCloud()) {
            holder.itemView.setBackgroundColor(holder.itemView.getResources().getColor(R.color.colorCloud));
        } else {
            holder.itemView.setBackgroundColor(holder.itemView.getResources().getColor(android.R.color.white));
        }
    }

    @Override
    public int getItemCount() {
        return records.length;
    }
}
