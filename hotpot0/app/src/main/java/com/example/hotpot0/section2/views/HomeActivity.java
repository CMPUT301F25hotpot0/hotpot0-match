package com.example.hotpot0.section2.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.EventDB;
import com.example.hotpot0.models.EventUserLink;
import com.example.hotpot0.models.EventUserLinkDB;
import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.models.Status;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * Home activity that displays the user's events in three categories:
 * confirmed, pending, and past. Also provides navigation to other
 * main sections of the app via the bottom navigation bar.
 */
public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private ListView confirmedList, pendingList, pastList;
    private EventDB eventDB;
    private EventUserLinkDB eventUserLinkDB = new EventUserLinkDB();
    private int userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userID = getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("userID", -1);
        Toast .makeText(this, "Welcome User #" + userID, Toast.LENGTH_SHORT).show();
        setContentView(R.layout.section2_userhome_activity);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_events) {
                Intent intent = new Intent(HomeActivity.this, CreateEventActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }

            if (id == R.id.nav_search) {
                Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }

            if (id == R.id.nav_notifications) {
                Intent intent = new Intent(HomeActivity.this, NotificationsActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }

            if (id == R.id.nav_profile) {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }

            return false;
        });

        confirmedList = findViewById(R.id.confirmed_list);
        pendingList = findViewById(R.id.pending_list);
        pastList = findViewById(R.id.past_list);

        eventDB = new EventDB();

        loadUserEvents();
    }

    /**
     * Ensures the Home tab remains selected when returning
     * to this activity.
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Ensures Home tab stays selected when coming back
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    /**
     * Loads all events from the database and sorts them into
     * confirmed, pending, and past categories based on the
     * user's EventUserLink status and whether the event is active.
     */
    private void loadUserEvents() {
        eventDB.getAllEvents(new EventDB.GetCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> allEvents) {
                List<Event> confirmed = new ArrayList<>();
                List<Event> pending = new ArrayList<>();
                List<Event> past = new ArrayList<>();

                List<String> confirmedStatuses = new ArrayList<>();
                List<String> pendingStatuses = new ArrayList<>();
                List<String> pastStatuses = new ArrayList<>();

                if (allEvents.isEmpty()) {
                    Toast.makeText(HomeActivity.this, "No events found", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (Event event : allEvents) {
                    String linkID = event.getEventID() + "_" + userID;

                    eventUserLinkDB.getEventUserLinkByID(linkID, new EventUserLinkDB.GetCallback<EventUserLink>() {
                        @Override
                        public void onSuccess(EventUserLink link) {
                            if (link == null || link.getStatus() == null) return;

                            String status = link.getStatus();
                            boolean isActive = event.getIsEventActive() != null && event.getIsEventActive();

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
                            else if ((status.equals("Accepted") && !isActive) || status.equals("Declined") ||
                                    status.equals("Cancelled") || (status.equals("Organizer") && !isActive)) {
                                past.add(event);
                                pastStatuses.add(status);
                            }

                            // Update adapters each time a new event's link is retrieved
                            runOnUiThread(() -> {
                                // Pass the userID to the adapter
                                confirmedList.setAdapter(new EventBlobAdapter(HomeActivity.this, confirmed, confirmedStatuses, userID));
                                pendingList.setAdapter(new EventBlobAdapter(HomeActivity.this, pending, pendingStatuses, userID));
                                pastList.setAdapter(new EventBlobAdapter(HomeActivity.this, past, pastStatuses, userID));
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // No EventUserLink found — ignore quietly
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(HomeActivity.this, "Error loading events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
