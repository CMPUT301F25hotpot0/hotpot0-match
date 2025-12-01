package com.example.hotpot0.section3.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.hotpot0.R;
import com.example.hotpot0.models.Event;

import java.util.List;

/**
 * Adapter for displaying events in a list for admin users.
 */
public class AdminEventAdapter extends ArrayAdapter<Event> {

    private final List<Event> eventList;
    private final Context context;

    public AdminEventAdapter(@NonNull Context context, List<Event> events) {
        super(context, 0, events);
        this.eventList = events;
        this.context = context;
    }

    /**
     * Provides a view for an AdapterView (ListView, GridView, etc.)
     *
     * @param position    The position of the item within the adapter's data set of the item whose view we want.
     * @param convertView The old view to reuse, if possible.
     * @param parent      The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.admin_event_blob, parent, false);
        }

        Event event = eventList.get(position);

        ImageView icon = convertView.findViewById(R.id.event_icon);
        TextView eventName = convertView.findViewById(R.id.eventNameTextView);
        TextView eventDesc = convertView.findViewById(R.id.eventDescriptionTextView);

        // Optional: Set admin icon for events
        icon.setImageResource(R.drawable.event_organizer);

        eventName.setText(event.getName());
        eventDesc.setText("Tap to manage event");

        return convertView;
    }
}

