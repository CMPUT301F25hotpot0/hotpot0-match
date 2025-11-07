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

/**
 * Custom ArrayAdapter for displaying a list of Event objects in a ListView.
 * Each list item shows the event name and the user's status for that event.
 * Clicking an item navigates the user to the detailed Event activity.
 */
public class EventBlobAdapter extends ArrayAdapter<Event> {

    private final List<String> statuses;
    private final int userID;
    private final EventActivityController controller;

    /**
     * Constructor for EventBlobAdapter.
     *
     * @param context  The current context.
     * @param events   List of Event objects to display.
     * @param statuses List of statuses corresponding to each event (e.g., "Accepted", "Declined").
     * @param userID   The ID of the current user.
     */
    public EventBlobAdapter(@NonNull Context context, List<Event> events, List<String> statuses, int userID) {
        super(context, 0, events);
        this.statuses = statuses;
        this.userID = userID;
        this.controller = new EventActivityController(context); // Pass context to controller
    }

    /**
     * Provides a view for an AdapterView (ListView) displaying an Event.
     *
     * @param position    The position of the item within the adapter's data set.
     * @param convertView The old view to reuse, if possible.
     * @param parent      The parent view that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
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
