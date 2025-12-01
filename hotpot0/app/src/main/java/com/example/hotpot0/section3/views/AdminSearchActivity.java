package com.example.hotpot0.section3.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.EventDB;
import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.models.UserProfile;
import com.example.hotpot0.section3.views.AdminEventAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for admin to search and manage events and user profiles.
 */
public class AdminSearchActivity extends AppCompatActivity {

    private EventDB eventDB;
    private ListView listView;
    private EditText searchBar;
    private Button eventsButton, profilesButton;

    private List<Event> allEvents = new ArrayList<>();
    private List<Event> filteredEvents = new ArrayList<>();
    private AdminEventAdapter adapter;

    private List<UserProfile> allProfiles = new ArrayList<>();
    private List<UserProfile> filteredProfiles = new ArrayList<>();
    private AdminProfileAdapter profileAdapter;
    private boolean showingProfiles = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section3_adminsearch_activity);

        eventDB = new EventDB();

        listView = findViewById(R.id.searchResultsListView);
        searchBar = findViewById(R.id.searchEditText);
        eventsButton = findViewById(R.id.eventsButton);
        profilesButton = findViewById(R.id.profilesButton);

        loadEvents();
        loadProfiles();
        setupSearchFilter();

        eventsButton.setOnClickListener(v -> {
            showingProfiles = false;
            listView.setAdapter(adapter);  // Show events
            adapter.notifyDataSetChanged();
        });

        profilesButton.setOnClickListener(v -> {
            showingProfiles = true;
            listView.setAdapter(profileAdapter); // Show profiles
            profileAdapter.notifyDataSetChanged();
        });



        setupItemClick();

        // Set Home as selected by default
        BottomNavigationView bottomNav = findViewById(R.id.adminBottomNavigationView);

        bottomNav.setSelectedItemId(R.id.admin_search);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.admin_search) {
                // Already on search
                return true;
            } else if (id == R.id.admin_home) {
                Intent searchIntent = new Intent(AdminSearchActivity.this, AdminHomeActivity.class);
                startActivity(searchIntent);
//                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                return true;
            } else if (id == R.id.admin_images) {
                startActivity(new Intent(AdminSearchActivity.this, AdminImageActivity.class));
//                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                return true;
            } else if (id == R.id.admin_notif) {
                startActivity(new Intent(AdminSearchActivity.this, AdminNotificationsActivity.class));
//                overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_in_left);
                return true;
            } else if (id == R.id.admin_settings) {
                startActivity(new Intent(AdminSearchActivity.this, AdminSettingsActivity.class));
//                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                return true;
            }
            return false;
        });
    }

    /**
     * Loads all events from the database and initializes the event adapter.
     */
    private void loadEvents() {
        eventDB.getAllEvents(new EventDB.GetCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> events) {
                allEvents = events;
                filteredEvents = new ArrayList<>(events);

                adapter = new AdminEventAdapter(AdminSearchActivity.this, filteredEvents);
                listView.setAdapter(adapter);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminSearchActivity.this, "Failed: " + e, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Loads all user profiles from the database and initializes the profile adapter.
     */
    private void loadProfiles() {
        ProfileDB profileDB = new ProfileDB();

        profileDB.getAllProfiles(new ProfileDB.GetCallback<List<UserProfile>>() {
            @Override
            public void onSuccess(List<UserProfile> profiles) {
                allProfiles = profiles;
                filteredProfiles = new ArrayList<>(profiles);

                profileAdapter = new AdminProfileAdapter(AdminSearchActivity.this, filteredProfiles);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminSearchActivity.this, "Failed: " + e, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sets up the search bar to filter events or profiles based on user input.
     */
    private void setupSearchFilter() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim().toLowerCase();

                if (!showingProfiles) {
                    filteredEvents.clear();
                    if (!allEvents.isEmpty()) {
                        for (Event event : allEvents) {
                            if (event.getName().toLowerCase().contains(query) || event.getDescription().toLowerCase().contains(query)) {
                                filteredEvents.add(event);
                            }
                        }
                        adapter.notifyDataSetChanged();

                    }
                } else {
                    filteredProfiles.clear();
                    if (!allProfiles.isEmpty()) {

                        for (UserProfile profile : allProfiles) {
                            if (profile.getName().toLowerCase().contains(query) || query.contains(profile.getUserID().toString())) {
                                filteredProfiles.add(profile);
                            }
                        }
                        profileAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    /**
     * Sets up item click listener for the list view to navigate to event or profile management.
     */
    private void setupItemClick() {
        listView.setOnItemClickListener((parent, view, position, id) -> {

            if (!showingProfiles) {
                Event selected = filteredEvents.get(position);

                Intent intent = new Intent(AdminSearchActivity.this, ManageEventActivity.class);
                intent.putExtra("eventID", selected.getEventID());
                startActivity(intent);
            } else {
                UserProfile selected = filteredProfiles.get(position);
                Intent intent = new Intent(AdminSearchActivity.this, AdminProfileActivity.class);
                Log.d("AdminSearchActivity", "Passing profileID: " + selected.getUserID());
                intent.putExtra("profileID", selected.getUserID());
                startActivity(intent);
            }
        });
    }
}

