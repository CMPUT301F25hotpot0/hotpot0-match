package com.example.hotpot0.section2.views;

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

public class ProfileBlobAdapter extends ArrayAdapter<UserProfile> {

    public ProfileBlobAdapter(@NonNull Context context, @NonNull List<UserProfile> profiles) {
        super(context, 0, profiles);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.profile_blob, parent, false);

        UserProfile profile = getItem(position);

        ImageView profileIcon = convertView.findViewById(R.id.profileIcon);
        TextView profileName = convertView.findViewById(R.id.profile_name);

        if (profile != null) {
            profileName.setText(profile.getName());
            // If you have profile pictures in the model, load here (e.g., Glide or default icon)
            profileIcon.setImageResource(R.drawable.ic_profile);
        }

        return convertView;
    }
}
