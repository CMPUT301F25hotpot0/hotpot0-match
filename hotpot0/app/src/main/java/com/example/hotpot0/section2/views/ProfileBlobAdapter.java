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

/**
 * Custom {@link ArrayAdapter} to display user profiles in a ListView.
 * <p>
 * Each list item (blob) shows the user's profile icon and name.
 * </p>
 */
public class ProfileBlobAdapter extends ArrayAdapter<UserProfile> {

    /**
     * Constructs a new {@code ProfileBlobAdapter}.
     *
     * @param context  the current context
     * @param profiles the list of {@link UserProfile} objects to display
     */
    public ProfileBlobAdapter(@NonNull Context context, @NonNull List<UserProfile> profiles) {
        super(context, 0, profiles);
    }

    /**
     * Provides a view for an AdapterView (ListView, GridView, etc.)
     * for the given position in the dataset.
     *
     * @param position    the position of the item within the adapter's data set
     * @param convertView the old view to reuse, if possible
     * @param parent      the parent view that this view will eventually be attached to
     * @return a {@link View} corresponding to the data at the specified position
     */
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.admin_profile_blob, parent, false);

        UserProfile profile = getItem(position);

        ImageView profileIcon = convertView.findViewById(R.id.profileIcon);
        TextView profileName = convertView.findViewById(R.id.profileNameTextView);

        if (profile != null) {
            profileName.setText(profile.getName());
            // If you have profile pictures in the model, load here
            profileIcon.setImageResource(R.drawable.ic_profile);
        }

        return convertView;
    }
}
