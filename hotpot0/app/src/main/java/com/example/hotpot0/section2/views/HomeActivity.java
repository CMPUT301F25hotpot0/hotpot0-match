package com.example.hotpot0.section2.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.EventDB;
import com.example.hotpot0.models.Status;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private ListView confirmedList, pendingList, pastList;
    private EventDB eventDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int userID = getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("userID", -1);
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

        eventDB.getAllEvents(new EventDB.GetCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> events) {
                List<Event> confirmedEvents = new ArrayList<>();
                List<Event> pendingEvents = new ArrayList<>();
                List<Event> pastEvents = new ArrayList<>();

                for (Event event : events) {
                    // Example placeholder logic; later this can come from Firestore “Status”
                    Status status = new Status();
                    try {
                        // Dummy placeholder for demo
                        String dummyStatus = "Accepted"; // Replace later with user-specific status
                        status.setStatus(dummyStatus);

                        if ((status.getStatus().equals("Accepted") && event.getIsEventActive()))
                            confirmedEvents.add(event);
                        else if (status.getStatus().equals("inWaitList") || status.getStatus().equals("Sampled"))
                            pendingEvents.add(event);
                        else if ((status.getStatus().equals("Accepted") && !event.getIsEventActive()) ||
                                status.getStatus().equals("Declined") || status.getStatus().equals("Cancelled"))
                            pastEvents.add(event);

                    } catch (Exception ignored) {}
                }

                confirmedList.setAdapter(new EventBlobAdapter(HomeActivity.this, confirmedEvents, "Confirmed"));
                pendingList.setAdapter(new EventBlobAdapter(HomeActivity.this, pendingEvents, "Pending"));
                pastList.setAdapter(new EventBlobAdapter(HomeActivity.this, pastEvents, "Past"));
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(HomeActivity.this, "Failed to load events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }
}
