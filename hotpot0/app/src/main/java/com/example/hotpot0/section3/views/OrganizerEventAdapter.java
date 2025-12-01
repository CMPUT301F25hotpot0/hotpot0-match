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

public class OrganizerEventAdapter extends ArrayAdapter<Event> {

    public OrganizerEventAdapter(@NonNull Context context, @NonNull List<Event> events) {
        super(context, 0, events);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.admin_event_blob, parent, false);
        }

        Event event = getItem(position);

        ImageView icon = convertView.findViewById(R.id.event_icon);
        TextView name = convertView.findViewById(R.id.eventNameTextView);
        TextView desc = convertView.findViewById(R.id.eventDescriptionTextView);

        // You can set a different icon if you want, or reuse the same
        if (icon != null) {
            icon.setImageResource(R.drawable.event_organizer); // or some organizer icon
        }

        if (event != null) {
            if (name != null) {
                name.setText(event.getName() != null ? event.getName() : "Unnamed event");
            }
            if (desc != null) {
                desc.setText("Tap to manage organizer");
            }
        }

        // NOTE: we DO NOT handle clicks here.
        // AdminSearchActivity's ListView onItemClick handles navigation
        // based on which chip is selected.

        return convertView;
    }
}

