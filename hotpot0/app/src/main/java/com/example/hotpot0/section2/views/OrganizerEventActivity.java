package com.example.hotpot0.section2.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.util.List;

/**
 * Activity that allows event organizers to view and manage their events.
 * <p>
 * Provides two layouts:
 * <ul>
 *     <li>Pre-draw layout: before any participants are sampled.</li>
 *     <li>Post-draw layout: after a random sample of participants has been selected.</li>
 * </ul>
 * <p>
 * Implements the following functionalities:
 * <ul>
 *     <li>Viewing event details</li>
 *     <li>Viewing waitlist and entrants</li>
 *     <li>Generating a random sample of participants</li>
 *     <li>Confirming entrants and filling empty spots</li>
 *     <li>Navigation via bottom navigation bar</li>
 * </ul>
 */
public class OrganizerEventActivity extends AppCompatActivity {

    private EventDB eventDB = new EventDB();
    private ProfileDB profileDB = new ProfileDB();
    private EventUserLinkDB eventUserLinkDB = new EventUserLinkDB();
    private EventActionHandler eventHandler = new EventActionHandler();

    private int eventID;
    private Event currentEvent;
    private EventUserLink currentEventUserLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int userID = getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("userID", -1);
        eventID = getIntent().getIntExtra("event_id", -1);

        String linkID = eventID + "_" + userID;

        // Fetch EventUserLink first
        eventUserLinkDB.getEventUserLinkByID(linkID, new EventUserLinkDB.GetCallback<EventUserLink>() {
            @Override
            public void onSuccess(EventUserLink eventUserLink) {
                if (eventUserLink == null) {
                    Toast.makeText(OrganizerEventActivity.this, "Event link not found.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                currentEventUserLink = eventUserLink;

                // Then fetch Event
                eventDB.getEventByID(eventID, new EventDB.GetCallback<Event>() {
                    @Override
                    public void onSuccess(Event event) {
                        if (event == null) {
                            Toast.makeText(OrganizerEventActivity.this, "Event not found.", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        currentEvent = event;

                        // Decide which layout to show
                        if (event.getSampledIDs().isEmpty()) {
                            setContentView(R.layout.section2_organizereventview_activity);
                            setupPreDrawLayout();
                        } else {
                            setContentView(R.layout.section2_organizereventview_postdraw);
                            setupPostDrawLayout();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(OrganizerEventActivity.this, "Error loading event details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(OrganizerEventActivity.this, "Error loading event link: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    // ------------------ PRE-DRAW LAYOUT ------------------
    private void setupPreDrawLayout() {
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
        Button generateSampleButton = findViewById(R.id.generate_sample_button);
        LinearLayout entrantsContainer = findViewById(R.id.entrants_container);

        // --- Populate Event info ---
        previewEventName.setText(currentEvent.getName());
        previewDescription.setText(currentEvent.getDescription());
        previewGuidelines.setText(currentEvent.getGuidelines());
        previewLocation.setText("Location: " + currentEvent.getLocation());
        previewTimeAndDay.setText("Time: " + currentEvent.getTime());
        previewDateRange.setText("Date: " + currentEvent.getStartDate());
        previewDuration.setText("Duration: " + currentEvent.getDuration());
        previewPrice.setText("Price: $" + currentEvent.getPrice());
        previewSpotsOpen.setText("Spots Open: " + currentEvent.getCapacity());
        previewDaysLeft.setText("Registration Period: " + currentEvent.getRegistrationEnd());

        // --- Load waitlist users ---
        eventUserLinkDB.getWaitListUsers(currentEvent.getLinkIDs(), new EventUserLinkDB.GetCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> waitListLinkIDs) {
                currentlyWaiting.setText("Currently Waiting: " + waitListLinkIDs.size());
                populateEntrants(entrantsContainer, waitListLinkIDs);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(OrganizerEventActivity.this, "Failed to load waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // TODO: Replace placeholders with actual image loading
        eventImage.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        qrCodeImage.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        mapPreview.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

        // --- Generate Sample Button ---
        generateSampleButton.setOnClickListener(v -> {
            Toast.makeText(this, "Generating random sample...", Toast.LENGTH_SHORT).show();

            eventDB.sampleEvent(currentEvent, new EventDB.GetCallback<List<String>>() {
                @Override
                public void onSuccess(List<String> sampledUsers) {

                    // Change status of EventUserLinks to "Sampled"
                    for (String linkID : sampledUsers) {
                        eventUserLinkDB.getEventUserLinkByID(linkID, new EventUserLinkDB.GetCallback<EventUserLink>() {
                            @Override
                            public void onSuccess(EventUserLink eventUserLink) {
                                if (eventUserLink != null) {
                                    // Update status to "Sampled"
                                    eventUserLink.setStatus("Sampled");
                                    eventUserLinkDB.updateEventUserLink(eventUserLink, new EventUserLinkDB.ActionCallback() {
                                        @Override
                                        public void onSuccess() {
                                            ;
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            Toast.makeText(
                                                    OrganizerEventActivity.this,
                                                    "Error updating link " + linkID + ": " + e.getMessage(),
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        }
                                    });
                                } else {
                                    Toast.makeText(
                                            OrganizerEventActivity.this,
                                            "EventUserLink not found for " + linkID,
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(
                                        OrganizerEventActivity.this,
                                        "Error fetching EventUserLink " + linkID + ": " + e.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });
                    }


                    Toast.makeText(OrganizerEventActivity.this,
                            "Sample generated successfully! Total: " + sampledUsers.size(),
                            Toast.LENGTH_SHORT).show();

                    // Switch to Post-Draw layout
                    setContentView(R.layout.section2_organizereventview_postdraw);
                    setupPostDrawLayout();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(OrganizerEventActivity.this, "Error generating sample: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        setupBottomNavigation();
    }

    // ------------------ POST-DRAW LAYOUT ------------------
    /**
     * Sets up the post-draw layout for organizers.
     * Displays event details, sampled entrants, cancelled entrants, and allows confirming entrants or filling empty spots.
     */
    private void setupPostDrawLayout() {
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

        // --- Populate Event info ---
        previewEventName.setText(currentEvent.getName());
        previewDescription.setText(currentEvent.getDescription());
        previewGuidelines.setText(currentEvent.getGuidelines());
        previewLocation.setText("Location: " + currentEvent.getLocation());
        previewTimeAndDay.setText("Time: " + currentEvent.getTime());
        previewDateRange.setText("Date: " + currentEvent.getStartDate());
        previewDuration.setText("Duration: " + currentEvent.getDuration());
        previewPrice.setText("Price: $" + currentEvent.getPrice());
        previewSpotsOpen.setText("Spots Open: " + currentEvent.getCapacity());
        previewDaysLeft.setText("Registration Period: " + currentEvent.getRegistrationEnd());

        // --- Populate sampled entrants ---
        populateEntrants(sampledEntrantsContainer, currentEvent.getSampledIDs());


        populateEntrants(cancelledEntrantsContainer, currentEvent.getCancelledIDs());
        populateEntrants(allEntrantsContainer, currentEvent.getLinkIDs());

        // --- Button actions ---
        buttonBack.setOnClickListener(v -> finish());

        buttonFillSpots.setOnClickListener(v -> {
            Toast.makeText(this, "Filling remaining spots...", Toast.LENGTH_SHORT).show();
            eventDB.fillEmptySampledSpots(currentEvent, new EventDB.GetCallback<List<String>>() {
                @Override
                public void onSuccess(List<String> newlySampledUsers) {

                    // Change status of EventUserLinks to "Sampled"
                    for (String linkID : newlySampledUsers) {
                        eventUserLinkDB.getEventUserLinkByID(linkID, new EventUserLinkDB.GetCallback<EventUserLink>() {
                            @Override
                            public void onSuccess(EventUserLink eventUserLink) {
                                if (eventUserLink != null) {
                                    // Update status to "Sampled"
                                    eventUserLink.setStatus("Sampled");
                                    eventUserLinkDB.updateEventUserLink(eventUserLink, new EventUserLinkDB.ActionCallback() {
                                        @Override
                                        public void onSuccess() {
                                            ;
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            Toast.makeText(
                                                    OrganizerEventActivity.this,
                                                    "Error updating link " + linkID + ": " + e.getMessage(),
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        }
                                    });
                                } else {
                                    Toast.makeText(
                                            OrganizerEventActivity.this,
                                            "EventUserLink not found for " + linkID,
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(
                                        OrganizerEventActivity.this,
                                        "Error fetching EventUserLink " + linkID + ": " + e.getMessage(),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });
                    }


                    Toast.makeText(OrganizerEventActivity.this,
                            "Filled spots successfully! Total: " + newlySampledUsers.size(),
                            Toast.LENGTH_SHORT).show();

                    // Switch to Post-Draw layout
                    setContentView(R.layout.section2_organizereventview_postdraw);
                    setupPostDrawLayout();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(OrganizerEventActivity.this, "Error generating sample: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            buttonConfirm.setEnabled(true);
            // After filling, you could refresh UI:
            // setupPostDrawLayout();
        });

        buttonConfirm.setOnClickListener(v -> {
            Toast.makeText(this, "Entrants confirmed!", Toast.LENGTH_SHORT).show();
            // TODO: Call eventHandler.confirmEntrants(eventID)
            buttonConfirm.setEnabled(false);
        });

        setupBottomNavigation();
    }

    // ------------------ HELPER METHODS ------------------
    /**
     * Populates a LinearLayout with  profile blobs.
     *
     * @param container LinearLayout to populate
     * @param linkIDs   List of event-user link IDs
     */
    private void populateEntrants(LinearLayout container, List<String> linkIDs) {
        container.removeAllViews();
        if (linkIDs == null || linkIDs.isEmpty()) {
            TextView noEntrantsText = new TextView(this);
            noEntrantsText.setText("No entrants found.");
            container.addView(noEntrantsText);
            return;
        }

        int organizerId = getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("userID", -1);

        for (String id : linkIDs) {
            String userID = id.split("_")[1];
            if (Integer.parseInt(userID) == organizerId) {
                continue; // Skip organizer's own profile
            }
            profileDB.getUserByID(Integer.parseInt(userID), new ProfileDB.GetCallback<UserProfile>() {
                @Override
                public void onSuccess(UserProfile profile) {
                    View blobView = LayoutInflater.from(OrganizerEventActivity.this)
                            .inflate(R.layout.profile_blob, container, false);
                    ((TextView) blobView.findViewById(R.id.profile_name)).setText(profile.getName());
                    ((ImageView) blobView.findViewById(R.id.profileIcon)).setImageResource(R.drawable.ic_profile);
                    container.addView(blobView);
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(OrganizerEventActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Sets up the bottom navigation bar with click listeners to navigate to other main activities.
     */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav == null) return;

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
}