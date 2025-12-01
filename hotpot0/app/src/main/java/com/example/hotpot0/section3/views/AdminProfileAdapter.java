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

/**
 * Adapter for displaying user profiles in a list for admin users.
 */
public class AdminProfileAdapter extends ArrayAdapter<UserProfile> {
    private List<UserProfile> profileList;
    private Context context;

    public AdminProfileAdapter(@NonNull Context context, List<UserProfile> profiles) {
        super(context, 0, profiles);
        this.profileList = profiles;
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
