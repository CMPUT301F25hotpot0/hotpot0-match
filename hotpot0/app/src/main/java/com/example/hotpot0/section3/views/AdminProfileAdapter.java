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

public class AdminProfileAdapter extends ArrayAdapter<UserProfile> {
    private List<UserProfile> profileList;
    private Context context;

    public AdminProfileAdapter(@NonNull Context context, List<UserProfile> profiles) {
        super(context, 0, profiles);
        this.profileList = profiles;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.admin_profile_blob, parent, false);
        }

        UserProfile profile = profileList.get(position);

        ImageView icon = convertView.findViewById(R.id.profileIcon);
        TextView nameText = convertView.findViewById(R.id.profileNameTextView);

        icon.setImageResource(R.drawable.ic_profile);
        nameText.setText(profile.getName());

        return convertView;
    }
}
