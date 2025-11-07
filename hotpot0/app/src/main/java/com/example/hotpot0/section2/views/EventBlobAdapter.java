package com.example.hotpot0.section2.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import com.example.hotpot0.R;
import com.example.hotpot0.models.Event;
import java.util.List;

public class EventBlobAdapter extends ArrayAdapter<Event> {
    private final Context context;
    private final List<Event> eventList;
    private final String statusLabel;

    public EventBlobAdapter(Context context, List<Event> events, String statusLabel) {
        super(context, 0, events);
        this.context = context;
        this.eventList = events;
        this.statusLabel = statusLabel;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.event_blob, parent, false);
        }

        Event event = eventList.get(position);
        TextView eventName = convertView.findViewById(R.id.event_name);
        TextView eventRole = convertView.findViewById(R.id.event_role);
        ImageView arrowIcon = convertView.findViewById(R.id.event_arrow);

        eventName.setText(event.getName());
        eventRole.setText(statusLabel);

        convertView.setOnClickListener(v -> {
            Toast.makeText(context, "Opening " + event.getName(), Toast.LENGTH_SHORT).show();
            // Add activity here later
        });

        return convertView;
    }
}

