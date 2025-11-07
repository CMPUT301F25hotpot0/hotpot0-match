package com.example.hotpot0.section2.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import com.example.hotpot0.R;
import com.example.hotpot0.models.Event;
import com.example.hotpot0.section2.controllers.EventActivityController;
import java.util.List;

public class EventBlobAdapter extends ArrayAdapter<Event> {

    private final List<String> statuses;
    private final int userID;
    private final EventActivityController controller;

    public EventBlobAdapter(@NonNull Context context, List<Event> events, List<String> statuses, int userID) {
        super(context, 0, events);
        this.statuses = statuses;
        this.userID = userID;
        this.controller = new EventActivityController(context); // Pass context to controller
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.event_blob, parent, false);

        Event event = getItem(position);
        String status = statuses.get(position);

        TextView eventName = convertView.findViewById(R.id.event_name);
        TextView eventRole = convertView.findViewById(R.id.event_role);
        ImageView icon = convertView.findViewById(R.id.event_arrow);

        if (event != null) {
            eventName.setText(event.getName());
            eventRole.setText(status);
        }

        // Set the click listener to navigate to the event activity
        convertView.setOnClickListener(v -> {
            // Call the controller to navigate to the correct activity
            if (event != null) {
                controller.navigateToEventActivity(event.getEventID(), userID);
            }
        });

        return convertView;
    }
}
