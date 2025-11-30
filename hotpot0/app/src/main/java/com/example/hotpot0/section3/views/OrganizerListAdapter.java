package com.example.hotpot0.section3.views;

import android.content.Context;
import android.content.Intent;
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

    public OrganizerListAdapter(@NonNull Context context, List<UserProfile> organizers) {
        super(context, 0, organizers);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.admin_event_blob, parent, false);
        }

        UserProfile organizer = getItem(position);

        ImageView icon = convertView.findViewById(R.id.event_icon);
        TextView name = convertView.findViewById(R.id.eventNameTextView);
        TextView desc = convertView.findViewById(R.id.eventDescriptionTextView);

        icon.setImageResource(R.drawable.event_organizer);

        if (organizer != null) {
            name.setText(organizer.getName());
            desc.setText("Organizer ID: " + organizer.getUserID());
        }

        convertView.setOnClickListener(v -> {
            if (organizer == null) return;
            Intent intent = new Intent(getContext(), AdminOrganizerViewActivity.class);
            intent.putExtra("organizerID", organizer.getUserID());
            getContext().startActivity(intent);
        });

        return convertView;
    }
}