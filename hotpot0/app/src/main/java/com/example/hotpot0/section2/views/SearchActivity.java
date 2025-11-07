package com.example.hotpot0.section2.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.EventDB;
import com.example.hotpot0.models.EventUserLink;
import com.example.hotpot0.models.EventUserLinkDB;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private ListView eventListView;
    private SearchView searchView;
    private EventDB eventDB;
    private EventUserLinkDB eventUserLinkDB = new EventUserLinkDB();
    private int userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section2_searchevents_activity);

        // Initialize UI elements
        eventListView = findViewById(R.id.event_list_view);
        searchView = findViewById(R.id.searchView);
        bottomNav = findViewById(R.id.bottomNavigationView);

        userID = getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("userID", -1);

        eventDB = new EventDB();

        // Handle bottom navigation clicks
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(SearchActivity.this, HomeActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }
            if (id == R.id.nav_profile) {
                startActivity(new Intent(SearchActivity.this, ProfileActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }
            if (id == R.id.nav_notifications) {
                startActivity(new Intent(SearchActivity.this, NotificationsActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }
            if (id == R.id.nav_events) {
                startActivity(new Intent(SearchActivity.this, CreateEventActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }
            return false;
        });

        // Load all events on activity start
        loadAllEvents();

        // Search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterEvents(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterEvents(newText);
                return true;
            }
        });
    }

    public void loadAllEvents() {
        eventDB.getAllEvents(new EventDB.GetCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> allEvents) {
                List<Event> events = new ArrayList<>();
                List<String> statuses = new ArrayList<>();

                if (allEvents.isEmpty()) {
                    Toast.makeText(SearchActivity.this, "No events found", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (Event event : allEvents) {
                    String linkID = event.getEventID() + "_" + userID;

                    eventUserLinkDB.getEventUserLinkByID(linkID, new EventUserLinkDB.GetCallback<EventUserLink>() {
                        @Override
                        public void onSuccess(EventUserLink link) {
                            String status = "";  // Default empty status

                            if (link != null) {
                                // If the user has a link to the event, set the actual status
                                status = link.getStatus();
                            }

                            events.add(event);
                            statuses.add(status);  // Add the status (either empty or actual)

                            // Once all statuses are fetched, update the adapter
                            if (events.size() == allEvents.size()) {
                                runOnUiThread(() -> {
                                    eventListView.setAdapter(new EventBlobAdapter(SearchActivity.this, events, statuses, userID));
                                });
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // If no EventUserLink exists, treat as having no status (empty)
                            events.add(event);
                            statuses.add("");  // Empty status for non-linked events

                            // Once all events are processed, update the adapter
                            if (events.size() == allEvents.size()) {
                                runOnUiThread(() -> {
                                    eventListView.setAdapter(new EventBlobAdapter(SearchActivity.this, events, statuses, userID));
                                });
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(SearchActivity.this, "Error loading events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterEvents(String query) {
        eventDB.getAllEvents(new EventDB.GetCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> allEvents) {
                List<Event> filteredEvents = new ArrayList<>();
                List<String> statuses = new ArrayList<>();

                // Filter events based on the search query
                for (Event event : allEvents) {
                    if (event.getName().toLowerCase().contains(query.toLowerCase())) {
                        filteredEvents.add(event);
                        statuses.add("");  // Add a placeholder status for now
                    }
                }

                // Update the adapter with filtered events
                runOnUiThread(() -> {
                    EventBlobAdapter adapter = new EventBlobAdapter(SearchActivity.this, filteredEvents, statuses, userID);
                    eventListView.setAdapter(adapter);
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(SearchActivity.this, "Error filtering events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
