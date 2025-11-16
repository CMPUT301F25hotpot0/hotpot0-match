package com.example.hotpot0.section2.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.hotpot0.R;
import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.EventDB;
import com.example.hotpot0.models.EventUserLink;
import com.example.hotpot0.models.EventUserLinkDB;
import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.models.UserProfile;
import com.example.hotpot0.section2.controllers.EventActionHandler;
import com.example.hotpot0.section2.controllers.QRGenerator;
import com.google.android.material.bottomnavigation.BottomNavigationView;


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

    private int eventID;
    private Event currentEvent;
    private EventUserLink currentEventUserLink;

    TextView previewGeolocation;
    private EventDB eventDB = new EventDB();
    private ProfileDB profileDB = new ProfileDB();
    private EventUserLinkDB eventUserLinkDB = new EventUserLinkDB();
    private EventActionHandler eventHandler = new EventActionHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get UserID and EventID to display the required view
        int userID = getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("userID", -1);
        eventID = getIntent().getIntExtra("event_id", -1);

        // Generating EventUserLink linkID
        String linkID = eventID + "_" + userID;

        // Fetch EventUserLink Object
        eventUserLinkDB.getEventUserLinkByID(linkID, new EventUserLinkDB.GetCallback<EventUserLink>() {
            @Override
            public void onSuccess(EventUserLink eventUserLink) {
                // Check if eventUserLink is null
                if (eventUserLink == null) {
                    Toast.makeText(OrganizerEventActivity.this, "Event link not found.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                // Save current EventUserLink
                currentEventUserLink = eventUserLink;

                // Then fetch Event
                eventDB.getEventByID(eventID, new EventDB.GetCallback<Event>() {
                    @Override
                    public void onSuccess(Event event) {
                        // Check if event is null
                        if (event == null) {
                            Toast.makeText(OrganizerEventActivity.this, "Event not found.", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        // Save current Event
                        currentEvent = event;

                        // Decide which layout to show
                        if (event.getSampledIDs().isEmpty()) {
                            setContentView(R.layout.section2_organizereventview_activity);
                            setupPreDrawLayout();
                        } else {
                            setContentView(R.layout.section2_organizereventview_postdraw);
                            // setupPostDrawLayout();
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

        // View bindings
        ImageView eventImage = findViewById(R.id.eventImage);
        TextView previewEventName = findViewById(R.id.previewEventName);
        TextView previewDescription = findViewById(R.id.previewDescription);
        TextView previewGuidelines = findViewById(R.id.previewGuidelines);
        TextView previewLocation = findViewById(R.id.previewLocation);
        TextView previewTime = findViewById(R.id.previewTime);
        TextView previewDateRange = findViewById(R.id.previewDateRange);
        TextView previewDuration = findViewById(R.id.previewDuration);
        TextView previewPrice = findViewById(R.id.previewPrice);
        TextView previewSpotsOpen = findViewById(R.id.previewSpotsOpen);
        TextView previewWaitingListCapacity = findViewById(R.id.previewWaitingListCapacity);
        TextView previewCurrentlyWaiting = findViewById(R.id.previewCurrentlyWaiting);
        TextView previewDaysLeft = findViewById(R.id.previewDaysLeft);
        Button generateSampleButton = findViewById(R.id.generate_sample_button);
        LinearLayout entrantsContainer = findViewById(R.id.entrants_container);
        previewGeolocation = findViewById(R.id.GeolocationStatus);
        ImageView qrCodeImage = findViewById(R.id.qr_code_image);
        TextView mapTitle = findViewById(R.id.event_map_title);
        FrameLayout mapContainer = findViewById(R.id.map_container);

        // Populate Event Info
        previewEventName.setText(currentEvent.getName() != null ? currentEvent.getName() : "No name provided");
        previewDescription.setText(currentEvent.getDescription() != null ? currentEvent.getDescription() : "No description provided");
        previewGuidelines.setText(currentEvent.getGuidelines() != null ? currentEvent.getGuidelines() : "No guidelines provided");
        previewLocation.setText(currentEvent.getLocation() != null ? currentEvent.getLocation() : "No location provided");
        previewTime.setText(currentEvent.getTime() != null ? currentEvent.getTime() : "No time specified");
        // Handle date range
        String dateRange = buildDateRange(currentEvent.getStartDate(), currentEvent.getEndDate());
        previewDateRange.setText(dateRange);
        previewDuration.setText(currentEvent.getDuration() != null ? currentEvent.getDuration() : "No duration specified");
        // Handle price
        String priceText = formatPrice(currentEvent.getPrice().toString());
        previewPrice.setText(priceText);
        // Handle capacity
        String capacityText = formatCapacity(currentEvent.getCapacity().toString());
        previewSpotsOpen.setText(capacityText);
        // Handle waiting list
        String waitingListText = formatWaitingList(currentEvent.getLinkIDs().toString());
        previewWaitingListCapacity.setText(waitingListText);
        // Handle registration period
        String registrationText = buildRegistrationPeriod(currentEvent.getRegistrationStart(), currentEvent.getRegistrationEnd());
        previewDaysLeft.setText(registrationText);
        // Handle geolocation status
        updateGeolocationStatus(currentEvent.getGeolocationRequired());

        // Generate QR Code for the Event
        QRGenerator qrGenerator = new QRGenerator();
        Bitmap qrBitmap = qrGenerator.generateQR("event:"+currentEvent.getEventID()); // or any unique event data

        if (qrBitmap != null) {
            qrCodeImage.setImageBitmap(qrBitmap);
            qrCodeImage.setVisibility(View.VISIBLE);
        } else {
            qrCodeImage.setVisibility(View.GONE); // hide if QR generation failed
        }

        // Handle Event Image
        String imageURL = currentEvent.getImageURL();
        if (imageURL == null || imageURL.isEmpty()) {
            // Hide the ImageView if no image is available
            eventImage.setVisibility(View.GONE);
        } else {
            // Show the ImageView
            eventImage.setVisibility(View.VISIBLE);
            // Load image using Glide
            Glide.with(this)
                    .load(imageURL)
                    .placeholder(R.drawable.placeholder_image) // optional placeholder
                    .into(eventImage);
        }

        // Load waitlist users
        eventUserLinkDB.getWaitListUsers(currentEvent.getLinkIDs(), new EventUserLinkDB.GetCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> waitListLinkIDs) {
                // Get Number of People in Wait List
                String numWaitList = waitListLinkIDs.size() + "";
                previewCurrentlyWaiting.setText(numWaitList);
                // Hide mapContainer if no one is waiting or if current event has no geolocation enabled
                if (waitListLinkIDs.isEmpty() || !currentEvent.getGeolocationRequired()) {
                    mapContainer.setVisibility(View.GONE);
                    mapTitle.setVisibility(View.GONE);
                } else {
                    mapContainer.setVisibility(View.VISIBLE);
                    // TODO : NEED TO PLOT LOCATION ON MAP --- need to define lat/long attributes in EventUserLink object
                }
                // Populate Entrants Section
                populateEntrants(entrantsContainer, waitListLinkIDs);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(OrganizerEventActivity.this, "Failed to load waitlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Generate Sample Button
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
                    // setupPostDrawLayout();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(OrganizerEventActivity.this, "Error generating sample: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        // Setup Bottom Navigation
        setupBottomNavigation();
    }

    // ------------------ POST-DRAW LAYOUT ------------------
    /**
     * Sets up the post-draw layout for organizers.
     * Displays event details, sampled entrants, cancelled entrants, and allows confirming entrants or filling empty spots.
     */
//    private void setupPostDrawLayout() {
//        // --- View bindings ---
//        ImageView eventImage = findViewById(R.id.eventImage);
//        TextView previewEventName = findViewById(R.id.preview_event_name);
//        TextView previewDescription = findViewById(R.id.preview_description);
//        TextView previewGuidelines = findViewById(R.id.preview_guidelines);
//        TextView previewLocation = findViewById(R.id.preview_location);
//        TextView previewTimeAndDay = findViewById(R.id.preview_time_and_day);
//        TextView previewDateRange = findViewById(R.id.preview_date_range);
//        TextView previewDuration = findViewById(R.id.preview_duration);
//        TextView previewPrice = findViewById(R.id.preview_price);
//        TextView previewSpotsOpen = findViewById(R.id.preview_spots_open);
//        TextView previewDaysLeft = findViewById(R.id.preview_days_left);
//        TextView currentlyWaiting = findViewById(R.id.currently_waiting);
//
//        LinearLayout sampledEntrantsContainer = findViewById(R.id.sampled_entrants_container);
//        LinearLayout cancelledEntrantsContainer = findViewById(R.id.cancelled_entrants_container);
//        LinearLayout allEntrantsContainer = findViewById(R.id.all_entrants_container);
//
//        Button buttonFillSpots = findViewById(R.id.button_fillSpots);
//        Button buttonConfirm = findViewById(R.id.button_confirm);
//        Button buttonBack = findViewById(R.id.button_BackPostDraw);
//        ImageView mapPreview = findViewById(R.id.mapPreview);
//
//        // --- Populate Event info ---
//        previewEventName.setText(currentEvent.getName());
//        previewDescription.setText(currentEvent.getDescription());
//        previewGuidelines.setText(currentEvent.getGuidelines());
//        previewLocation.setText("Location: " + currentEvent.getLocation());
//        previewTimeAndDay.setText("Time: " + currentEvent.getTime());
//        // previewDateRange.setText("Date: " + currentEvent.getDate());
//        previewDuration.setText("Duration: " + currentEvent.getDuration());
//        previewPrice.setText("Price: $" + currentEvent.getPrice());
//        previewSpotsOpen.setText("Spots Open: " + currentEvent.getCapacity());
//        // previewDaysLeft.setText("Registration Period: " + currentEvent.getRegistration_period());
//
//        // --- Populate sampled entrants ---
//        populateEntrants(sampledEntrantsContainer, currentEvent.getSampledIDs());
//
//
//        populateEntrants(cancelledEntrantsContainer, currentEvent.getCancelledIDs());
//        populateEntrants(allEntrantsContainer, currentEvent.getLinkIDs());
//
//        // --- Button actions ---
//        buttonBack.setOnClickListener(v -> finish());
//
//        buttonFillSpots.setOnClickListener(v -> {
//            Toast.makeText(this, "Filling remaining spots...", Toast.LENGTH_SHORT).show();
//            eventDB.fillEmptySampledSpots(currentEvent, new EventDB.GetCallback<List<String>>() {
//                @Override
//                public void onSuccess(List<String> newlySampledUsers) {
//
//                    // Change status of EventUserLinks to "Sampled"
//                    for (String linkID : newlySampledUsers) {
//                        eventUserLinkDB.getEventUserLinkByID(linkID, new EventUserLinkDB.GetCallback<EventUserLink>() {
//                            @Override
//                            public void onSuccess(EventUserLink eventUserLink) {
//                                if (eventUserLink != null) {
//                                    // Update status to "Sampled"
//                                    eventUserLink.setStatus("Sampled");
//                                    eventUserLinkDB.updateEventUserLink(eventUserLink, new EventUserLinkDB.ActionCallback() {
//                                        @Override
//                                        public void onSuccess() {
//                                            ;
//                                        }
//
//                                        @Override
//                                        public void onFailure(Exception e) {
//                                            Toast.makeText(
//                                                    OrganizerEventActivity.this,
//                                                    "Error updating link " + linkID + ": " + e.getMessage(),
//                                                    Toast.LENGTH_SHORT
//                                            ).show();
//                                        }
//                                    });
//                                } else {
//                                    Toast.makeText(
//                                            OrganizerEventActivity.this,
//                                            "EventUserLink not found for " + linkID,
//                                            Toast.LENGTH_SHORT
//                                    ).show();
//                                }
//                            }
//
//                            @Override
//                            public void onFailure(Exception e) {
//                                Toast.makeText(
//                                        OrganizerEventActivity.this,
//                                        "Error fetching EventUserLink " + linkID + ": " + e.getMessage(),
//                                        Toast.LENGTH_SHORT
//                                ).show();
//                            }
//                        });
//                    }
//
//
//                    Toast.makeText(OrganizerEventActivity.this,
//                            "Filled spots successfully! Total: " + newlySampledUsers.size(),
//                            Toast.LENGTH_SHORT).show();
//
//                    // Switch to Post-Draw layout
//                    setContentView(R.layout.section2_organizereventview_postdraw);
//                    setupPostDrawLayout();
//                }
//
//                @Override
//                public void onFailure(Exception e) {
//                    Toast.makeText(OrganizerEventActivity.this, "Error generating sample: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//            buttonConfirm.setEnabled(true);
//            // After filling, you could refresh UI:
//            // setupPostDrawLayout();
//        });
//
//        buttonConfirm.setOnClickListener(v -> {
//            Toast.makeText(this, "Entrants confirmed!", Toast.LENGTH_SHORT).show();
//            // TODO: Call eventHandler.confirmEntrants(eventID)
//            buttonConfirm.setEnabled(false);
//        });
//
//        setupBottomNavigation();
//    }

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

    // HELPER METHODS

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
                            .inflate(R.layout.admin_profile_blob, container, false);
                    ((TextView) blobView.findViewById(R.id.profileNameTextView)).setText(profile.getName());
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

    private String buildDateRange(String startDate, String endDate) {
        if (startDate != null && endDate != null) {
            return startDate + " to " + endDate;
        } else if (startDate != null) {
            return startDate + " (Single day event)";
        } else {
            return "No dates specified";
        }
    }

    private String formatPrice(String price) {
        if (price == null || price.isEmpty()) {
            return "Free";
        }

        try {
            double priceValue = Double.parseDouble(price);
            if (priceValue == 0) {
                return "Free";
            } else {
                return String.format("$%.2f CAD", priceValue);
            }
        } catch (NumberFormatException e) {
            return "Invalid price";
        }
    }

    private String formatCapacity(String capacity) {
        if (capacity == null || capacity.isEmpty()) {
            return "Capacity not specified";
        }

        try {
            int capacityValue = Integer.parseInt(capacity);
            return capacityValue + " spots";
        } catch (NumberFormatException e) {
            return "Invalid capacity";
        }
    }

    private String formatWaitingList(String waitingList) {
        if (waitingList == null || waitingList.isEmpty() || waitingList.equals("0")) {
            return "No Cap on waiting list";
        }

        try {
            int waitingListValue = Integer.parseInt(waitingList);
            return waitingListValue + " waiting list spots";
        } catch (NumberFormatException e) {
            return "Invalid waiting list capacity";
        }
    }

    private String buildRegistrationPeriod(String registrationStart, String registrationEnd) {
        if (registrationStart != null && registrationEnd != null) {
            return registrationStart + " to " + registrationEnd;
        } else if (registrationStart != null) {
            return "Starts: " + registrationStart;
        } else {
            return "Registration period not specified";
        }
    }

    private void updateGeolocationStatus(boolean geolocationEnabled) {
        if (geolocationEnabled) {
            previewGeolocation.setText("NOTE: Geolocation tracking enabled");
            previewGeolocation.setVisibility(View.VISIBLE);
        } else {
            previewGeolocation.setVisibility(View.GONE);
        }
    }
}