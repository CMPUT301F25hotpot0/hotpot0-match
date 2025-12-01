package com.example.hotpot0.section3.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.EventDB;
import com.example.hotpot0.models.EventUserLinkDB;
import com.example.hotpot0.models.PicturesDB;
import com.example.hotpot0.models.ProfileDB;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Shows admin details on current activity of app.
 */
public class AdminHomeActivity extends AppCompatActivity {

    // Stats TextViews
    private TextView activeEventsCount, activeProfilesCount, activeOrganizersCount, imagesStoredCount;
    private EventDB eventDB;
    private ProfileDB profileDB;
    private PicturesDB picturesDB;
    private EventUserLinkDB eventUserLinkDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section3_adminhome_activity);

        // Retrieve adminID from the Intent if needed
        int adminID = getIntent().getIntExtra("adminID", -1);

        // Initialize TextViews
        activeEventsCount = findViewById(R.id.activeEventsCount);
        activeProfilesCount = findViewById(R.id.activeProfilesCount);
        activeOrganizersCount = findViewById(R.id.activeOrganizersCount);
        imagesStoredCount = findViewById(R.id.imagesStoredCount);

        eventDB = new EventDB();
        profileDB = new ProfileDB();
        picturesDB = new PicturesDB();
        eventUserLinkDB = new EventUserLinkDB();

        loadStats();

        // Set Home as selected by default
        BottomNavigationView bottomNav = findViewById(R.id.adminBottomNavigationView);
        bottomNav.setSelectedItemId(R.id.admin_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.admin_home) {
                // Already on home
                return true;
            } else if (id == R.id.admin_search) {
                Intent searchIntent = new Intent(AdminHomeActivity.this, AdminSearchActivity.class);
                startActivity(searchIntent);
//                overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_in_left);
                return true;
            } else if (id == R.id.admin_images) {
                startActivity(new Intent(AdminHomeActivity.this, AdminImageActivity.class));
//                overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_in_left);
                return true;
            } else if (id == R.id.admin_notif) {
                startActivity(new Intent(AdminHomeActivity.this, AdminNotificationsActivity.class));
//                overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_in_left);
                return true;
            } else if (id == R.id.admin_settings) {
                startActivity(new Intent(AdminHomeActivity.this, AdminSettingsActivity.class));
//                overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_in_left);
                return true;
            }
            return false;
        });
    }

    private void loadStats() {
        // Active Events
        eventDB.getAllActiveEvents(new EventDB.GetCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> events) {
                activeEventsCount.setText(String.valueOf(events.size()));
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminHomeActivity.this, "Failed to load events", Toast.LENGTH_SHORT).show();
            }
        });

        // Active Profiles
        profileDB.getTotalUsers(new ProfileDB.GetCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                activeProfilesCount.setText(String.valueOf(count));
            }

            @Override
            public void onFailure(Exception e) {
                activeProfilesCount.setText("0");
                e.printStackTrace();
            }
        });

        // Active Organizers
        eventUserLinkDB.getOrganizers(new EventUserLinkDB.GetCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                activeOrganizersCount.setText(String.valueOf(count));
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminHomeActivity.this, "Failed to load organizers", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });

        // Images Stored
        picturesDB.getAllEventImages(new PicturesDB.Callback<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                imagesStoredCount.setText(String.valueOf(result.size()));
            }

            @Override
            public void onFailure(Exception e) {
                imagesStoredCount.setText("0");
            }
        });
    }
}


