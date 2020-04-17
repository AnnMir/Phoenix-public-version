package nsu.fit.g14201.marchenko.phoenix.contacts.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import nsu.fit.g14201.marchenko.phoenix.R;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.MyViewHolder>{

    private Map<String, String> contacts = new HashMap<>();
    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> numbers = new ArrayList<>();

    public void setItems(Map<String, String> c) {
        contacts = new TreeMap<String, String>(c);
        if(contacts.size()!=0){
            for (Map.Entry e: contacts.entrySet()) {
                names.add(e.getKey().toString());
                numbers.add(e.getValue().toString());
            }
        }
        notifyDataSetChanged();
    }

    public void clearItems() {
        contacts.clear();
        names.clear();
        numbers.clear();
        notifyDataSetChanged();
    }

    public String getNumber(int pos){
        return numbers.get(pos);
    }

    public String getName(int pos){
        return names.get(pos);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView nameView;
        private TextView numberView;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            this.nameView = itemView.findViewById(R.id.name);
            this.numberView = itemView.findViewById(R.id.number);
        }

        void bind(String name, String number) {
            nameView.setText(name);
            numberView.setText(number);
        }
    }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.contact_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            holder.bind(names.get(position), numbers.get(position));
        }

        @Override
        public int getItemCount() {
            return contacts.size();
        }
}
