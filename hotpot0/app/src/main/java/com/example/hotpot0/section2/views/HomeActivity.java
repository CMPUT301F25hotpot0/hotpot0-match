package com.example.hotpot0.section2.views;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.example.hotpot0.R;
import android.content.Intent;
import android.widget.ListView;
import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.EventDB;
import androidx.appcompat.app.AppCompatActivity;
import com.example.hotpot0.models.EventUserLink;
import com.example.hotpot0.models.EventUserLinkDB;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;
import java.util.ArrayList;

/**
 * Home activity that displays the user's events in three categories:
 * confirmed, pending, and past. Also provides navigation to other
 * main sections of the app via the bottom navigation bar.
 */
public class HomeActivity extends AppCompatActivity {

    // Class Variables
    private int userID;
    private EventDB eventDB;
    private EventUserLinkDB eventUserLinkDB = new EventUserLinkDB();

    // Android UI Elements
    private ListView confirmedList, pendingList, pastList;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve userID from SharedPreferences
        userID = getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("userID", -1);
        setContentView(R.layout.section2_userhome_activity);

        // Handling Bottom Navigation Bar
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        setupBottomNavigation();

        // Initialize ListViews
        confirmedList = findViewById(R.id.confirmed_list);
        pendingList   = findViewById(R.id.pending_list);
        pastList      = findViewById(R.id.past_list);

        // Initialize EventDB
        eventDB = new EventDB();

        // Load user events into the lists
        loadUserEvents();
    }

    /**
     * Loads all events from the database and sorts them into
     * confirmed, pending, and past categories based on the
     * user's EventUserLink status and whether the event is active.
     */
    private void loadUserEvents() {

        // Fetch all events from the database, Returns List of Event Objects
        eventDB.getAllEvents(new EventDB.GetCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> allEvents) {
                List<Event> confirmed = new ArrayList<>();
                List<Event> pending = new ArrayList<>();
                List<Event> past = new ArrayList<>();

                List<String> confirmedStatuses = new ArrayList<>();
                List<String> pendingStatuses = new ArrayList<>();
                List<String> pastStatuses = new ArrayList<>();

                // Handle case where no events are found
                if (allEvents.isEmpty()) {
                    Toast.makeText(HomeActivity.this, "No events found", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Iterate through all events to categorize them
                for (Event event : allEvents) {

                    // Construct linkID using eventID and userID to find the relevant EventUserLink
                    String linkID = event.getEventID() + "_" + userID;
                    // Fetch the EventUserLink for the current event and user, Returns EventUserLink Object
                    eventUserLinkDB.getEventUserLinkByID(linkID, new EventUserLinkDB.GetCallback<EventUserLink>() {

                        @Override
                        public void onSuccess(EventUserLink link) {

                            // If no link or status is found, skip this event
                            if (link == null || link.getStatus() == null) return;
                            // Determine the status and categorize the event accordingly
                            String status = link.getStatus();
                            boolean isActive = event.getIsEventActive() != null && event.getIsEventActive();

                            // Categorize event based on status and activity
                            if (!isActive) {
                                past.add(event);
                                pastStatuses.add(status);
                            } else if ((status.equals("Accepted") && isActive) || (status.equals("Organizer") && isActive)) {
                                confirmed.add(event);
                                confirmedStatuses.add(status);
                            }
                            else if (status.equals("inWaitList") || status.equals("Sampled")) {
                                pending.add(event);
                                pendingStatuses.add(status);
                            }
                            // Removed (status.equals("Accepted") && !isActive) & (status.equals("Organizer") && !isActive) - they are covered in the first if condition
                            else if (status.equals("Declined") || status.equals("Cancelled")) {
                                past.add(event);
                                pastStatuses.add(status);
                            }

                            // Update adapters each time a new event's link is retrieved
                            runOnUiThread(() -> {
                                confirmedList.setAdapter(new EventBlobAdapter(HomeActivity.this, confirmed, confirmedStatuses, userID));
                                expandListView(confirmedList);
                                fadeIn(confirmedList);

                                pendingList.setAdapter(new EventBlobAdapter(HomeActivity.this, pending, pendingStatuses, userID));
                                expandListView(pendingList);
                                fadeIn(pendingList);

                                pastList.setAdapter(new EventBlobAdapter(HomeActivity.this, past, pastStatuses, userID));
                                expandListView(pastList);
                                fadeIn(pastList);
                            });
                        }

                        // Handle failure to fetch EventUserLink from EventUserLinkDB
                        @Override
                        public void onFailure(Exception e) {
                            // No EventUserLink found — ignore quietly
                        }
                    });
                }
            }

            // Handle failure to fetch events from EventDB
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(HomeActivity.this, "Error loading events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**Refreshes the event data by clearing existing adapters
     * and reloading the user events.
     */
    private void refreshData() {
        // Clear existing lists and reload fresh data
        if (confirmedList != null) confirmedList.setAdapter(null);
        if (pendingList != null) pendingList.setAdapter(null);
        if (pastList != null) pastList.setAdapter(null);

        loadUserEvents(); // Your existing method
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data every time the activity comes to foreground
        loadUserEvents();
        // Refresh navigation state
        bottomNavigationView.setSelectedItemId(R.id.nav_home); // Assuming you have a home nav item
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        // Refresh your data
        refreshData();
    }

    private void setupBottomNavigation() {
        // Set default selected item
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        // Setting up listener for navigation item selection
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_events) {
                Intent intent = new Intent(HomeActivity.this, CreateEventActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            }

            if (id == R.id.nav_search) {
                Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            }

            if (id == R.id.nav_notifications) {
                Intent intent = new Intent(HomeActivity.this, NotificationsActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            }

            if (id == R.id.nav_profile) {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            }

            return false;
        });
    }

    /** Expands a ListView to fit all its items */
    private void expandListView(ListView listView) {
        android.widget.ListAdapter adapter = listView.getAdapter();
        if (adapter == null) return;

        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            android.view.View listItem = adapter.getView(i, null, listView);
            listItem.measure(
                    View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.UNSPECIFIED
            );
            totalHeight += listItem.getMeasuredHeight();
        }

        android.view.ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
    private void fadeIn(View view) {
        view.setAlpha(0f);
        view.animate().alpha(1f).setDuration(300).start();
    }
}