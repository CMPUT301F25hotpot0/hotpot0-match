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
import com.example.hotpot0.models.UserProfile;

import java.util.List;

public class OrganizerListAdapter extends ArrayAdapter<UserProfile> {

    private final Context context;
    private final List<UserProfile> organizers;

    public OrganizerListAdapter(@NonNull Context context, @NonNull List<UserProfile> organizers) {
        super(context, 0, organizers);
        this.context = context;
        this.organizers = organizers;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            // ✅ Use your existing event row layout
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.admin_event_blob, parent, false);
        }

        // These IDs come from admin_event_blob.xml
        ImageView icon = convertView.findViewById(R.id.event_icon);
        TextView name = convertView.findViewById(R.id.eventNameTextView);
        TextView desc = convertView.findViewById(R.id.eventDescriptionTextView);

        UserProfile organizer = organizers.get(position);

        // If you don't have event_organizer drawable, you can change this to another icon you DO have.
        icon.setImageResource(R.drawable.event_organizer);

        if (organizer != null) {
            name.setText(organizer.getName());
            desc.setText("Organizer ID: " + organizer.getUserID());
        } else {
            name.setText("");
            desc.setText("");
        }

        // ⚠ No click listener here – clicks are handled in AdminSearchActivity via setOnItemClickListener
        return convertView;
    }
}
