package com.example.hotpot0.section2.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.EventDB;
import com.example.hotpot0.models.EventUserLink;
import com.example.hotpot0.models.EventUserLinkDB;
import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.models.UserProfile;
import com.example.hotpot0.section2.controllers.EventActionHandler;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class OrganizerEventActivity extends AppCompatActivity {

    private EventDB eventDB = new EventDB();
    private ProfileDB profileDB = new ProfileDB();
    private EventUserLinkDB eventUserLinkDB = new EventUserLinkDB();
    private EventActionHandler eventHandler = new EventActionHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int userID = getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("userID", -1);
        int eventID = getIntent().getIntExtra("event_id", -1);

        // Build the linkID (e.g., "12_5" for event 12 and user 5)
        String linkID = eventID + "_" + userID;

        // Fetch EventUserLink from database
        eventUserLinkDB.getEventUserLinkByID(linkID, new EventUserLinkDB.GetCallback<EventUserLink>() {

            @Override
            public void onSuccess(EventUserLink eventUserLink) {

                if (eventUserLink == null) {
                    Toast.makeText(OrganizerEventActivity.this, "Event link not found.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                EventDB eventDB = new EventDB();

                eventDB.getEventByID(eventID, new EventDB.GetCallback<Event>() {

                    @Override
                    public void onSuccess(Event event) {

                        if (event != null) {

                            if (event.getSampledIDs().isEmpty()) {
                                setContentView(R.layout.section2_organizereventview_activity);
                                setupPreDrawLayout(eventUserLink, eventID);

                            } else if (!event.getSampledIDs().isEmpty()) {
                                setContentView(R.layout.section2_organizereventview_postdraw);
                                setupPostDrawLayout(eventUserLink);

                        }
                    }}
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(OrganizerEventActivity.this, "Error loading event details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(OrganizerEventActivity.this, "Error loading event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Setup logic for the Pre-Draw layout
     */
    private void setupPreDrawLayout(EventUserLink eventUserLink, int eventID) {
        // --- View bindings ---
        ImageView eventImage = findViewById(R.id.event_image);
        TextView previewEventName = findViewById(R.id.preview_event_name);
        TextView previewDescription = findViewById(R.id.preview_description);
        TextView previewGuidelines = findViewById(R.id.preview_guidelines);
        TextView previewLocation = findViewById(R.id.preview_location);
        TextView previewTimeAndDay = findViewById(R.id.preview_time_and_day);
        TextView previewDateRange = findViewById(R.id.preview_date_range);
        TextView previewDuration = findViewById(R.id.preview_duration);
        TextView previewPrice = findViewById(R.id.preview_price);
        TextView previewSpotsOpen = findViewById(R.id.preview_spots_open);
        TextView previewDaysLeft = findViewById(R.id.preview_days_left);
        TextView currentlyWaiting = findViewById(R.id.currently_waiting);
        ImageView qrCodeImage = findViewById(R.id.qr_code_image);
        ImageView mapPreview = findViewById(R.id.map_preview);
        // ListView entrantsListView = findViewById(R.id.entrants_list_view);
        Button generateSampleButton = findViewById(R.id.generate_sample_button);

        // --- Basic UI initialization ---
        eventDB.getEventByID(eventID, new EventDB.GetCallback<Event>() {

            @Override
            public void onSuccess(Event event) {

                if (event != null) {
                    previewEventName.setText(event.getName());
                    previewDescription.setText(event.getDescription());
                    previewGuidelines.setText(event.getGuidelines());
                    previewLocation.setText("Location: " + event.getLocation());

                    // Using getTime() since getTimeAndDay() doesn’t exist
                    previewTimeAndDay.setText("Time: " + event.getTime());

                    // Using getDate() instead of getDateRange()
                    previewDateRange.setText("Date: " + event.getDate());

                    previewDuration.setText("Duration: " + event.getDuration());
                    previewPrice.setText("Price: $" + event.getPrice());

                    // These two fields don’t exist — you can approximate or remove depending on your data source
                    previewSpotsOpen.setText("Spots Open: " + event.getCapacity());
                    previewDaysLeft.setText("Registration Period: " + event.getRegistration_period());

                    List<String> allLinkIDs = event.getLinkIDs();

                    eventUserLinkDB.getWaitListUsers(allLinkIDs, new EventUserLinkDB.GetCallback<List<String>>() {
                        @Override
                        public void onSuccess(List<String> waitListLinkIDs) {
                            currentlyWaiting.setText("Currently Waiting: " + waitListLinkIDs.size());
                            LinearLayout entrantsContainer = findViewById(R.id.entrants_container);
                            entrantsContainer.removeAllViews();

                            for (String id : waitListLinkIDs) {
                                String userID = id.split("_")[1]; // get second element
                                profileDB.getUserByID(Integer.parseInt(userID), new ProfileDB.GetCallback<UserProfile>() {
                                    @Override
                                    public void onSuccess(UserProfile profile) {
                                        View blobView = LayoutInflater.from(OrganizerEventActivity.this)
                                                .inflate(R.layout.profile_blob, entrantsContainer, false);

                                        TextView profileName = blobView.findViewById(R.id.profile_name);
                                        ImageView profileIcon = blobView.findViewById(R.id.profileIcon);

                                        profileName.setText(profile.getName());
                                        profileIcon.setImageResource(R.drawable.ic_profile);

                                        entrantsContainer.addView(blobView);
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Toast.makeText(OrganizerEventActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(OrganizerEventActivity.this, "Failed to load waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(OrganizerEventActivity.this, "Error loading event details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // TODO: Replace with actual image loading (e.g., from URL or FirebaseStorage)
        eventImage.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        qrCodeImage.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        mapPreview.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

        // --- Button click handling ---
        generateSampleButton.setOnClickListener(v -> {
            Toast.makeText(this, "Generating random sample of entrants...", Toast.LENGTH_SHORT).show();

            // First, fetch the Event object by ID
            eventDB.getEventByID(eventID, new EventDB.GetCallback<Event>() {
                @Override
                public void onSuccess(Event event) {
                    if (event == null) {
                        Toast.makeText(OrganizerEventActivity.this, "Event not found.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Now sample users asynchronously
                    eventDB.sampleEvent(event, new EventDB.GetCallback<List<String>>() {
                        @Override
                        public void onSuccess(List<String> sampledUsers) {
                            Toast.makeText(OrganizerEventActivity.this,
                                    "Sample generated successfully! Total sampled: " + sampledUsers.size(),
                                    Toast.LENGTH_SHORT).show();

                            // --- SWITCH TO POST-DRAW LAYOUT ---
                            setContentView(R.layout.section2_organizereventview_postdraw);
                            setupPostDrawLayout(eventUserLink); // <-- initialize all UI elements
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(OrganizerEventActivity.this,
                                    "Error generating sample: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(OrganizerEventActivity.this,
                            "Error fetching event: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });

        // --- Bottom Navigation setup (optional placeholder) ---
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(OrganizerEventActivity.this, HomeActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }
            if (id == R.id.nav_profile) {
                startActivity(new Intent(OrganizerEventActivity.this, ProfileActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }
            if (id == R.id.nav_notifications) {
                startActivity(new Intent(OrganizerEventActivity.this, NotificationsActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }
            if (id == R.id.nav_events) {
                startActivity(new Intent(OrganizerEventActivity.this, CreateEventActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }
            return false;
        });
    }

    /**
     * Setup logic for the Post-Draw layout
     */
    private void setupPostDrawLayout(EventUserLink eventUserLink) {
        // --- View bindings ---
        ImageView eventImage = findViewById(R.id.eventImage);
        TextView previewEventName = findViewById(R.id.preview_event_name);
        TextView previewDescription = findViewById(R.id.preview_description);
        TextView previewGuidelines = findViewById(R.id.preview_guidelines);
        TextView previewLocation = findViewById(R.id.preview_location);
        TextView previewTimeAndDay = findViewById(R.id.preview_time_and_day);
        TextView previewDateRange = findViewById(R.id.preview_date_range);
        TextView previewDuration = findViewById(R.id.preview_duration);
        TextView previewPrice = findViewById(R.id.preview_price);
        TextView previewSpotsOpen = findViewById(R.id.preview_spots_open);
        TextView previewDaysLeft = findViewById(R.id.preview_days_left);
        TextView currentlyWaiting = findViewById(R.id.currently_waiting);

        LinearLayout sampledEntrantsContainer = findViewById(R.id.sampled_entrants_container);
        LinearLayout cancelledEntrantsContainer = findViewById(R.id.cancelled_entrants_container);
        LinearLayout allEntrantsContainer = findViewById(R.id.all_entrants_container);

        Button buttonFillSpots = findViewById(R.id.button_fillSpots);
        Button buttonConfirm = findViewById(R.id.button_confirm);
        Button buttonBack = findViewById(R.id.button_BackPostDraw);
        ImageView mapPreview = findViewById(R.id.mapPreview);

        // --- Populate static placeholders ---
        previewEventName.setText("Post-Draw Event Preview");
        previewDescription.setText("Here’s your event’s post-draw details.");
        buttonConfirm.setEnabled(false); // Disabled until confirmation ready

        // --- Button interactions ---
        buttonBack.setOnClickListener(v -> finish());

        buttonFillSpots.setOnClickListener(v -> {
            Toast.makeText(this, "Filling remaining event spots...", Toast.LENGTH_SHORT).show();

            // TODO: integrate with controller later
            buttonConfirm.setEnabled(true);
        });

        buttonConfirm.setOnClickListener(v -> {
            Toast.makeText(this, "Confirmed selected entrants!", Toast.LENGTH_SHORT).show();

            // TODO: add eventHandler.confirmEntrants(eventID)
            buttonConfirm.setEnabled(false);
        });

        // --- Bottom Navigation setup (placeholder) ---
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    Toast.makeText(this, "Home clicked", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.nav_profile:
                    Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show();
                    return true;
                default:
                    return false;
            }
        });
    }
}