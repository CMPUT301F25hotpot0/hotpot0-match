package com.example.hotpot0.section2.views;

import android.annotation.SuppressLint;
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

import java.util.ArrayList;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
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
    private Event currentEvent = new Event();
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
                        if (event.getSampledIDs().isEmpty() && event.getCancelledIDs().isEmpty()) {
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

        if (currentEvent.getTotalSampled() != 0) {
            generateSampleButton.setEnabled(true);
        } else {
            generateSampleButton.setEnabled(false);
        }
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
        String spotsOpen = (currentEvent.getCapacity() - currentEvent.getTotalWaitlist()) == 0
                ? "All spots are filled!"
                : Integer.toString(currentEvent.getCapacity() - currentEvent.getTotalWaitlist());
        previewSpotsOpen.setText(spotsOpen);
        // Handle waiting list
        String waitingListText = formatWaitingList(currentEvent.getLinkIDs().toString());
        previewWaitingListCapacity.setText(waitingListText);
        // Handle registration period
        String registrationText = buildRegistrationPeriod(currentEvent.getRegistrationStart(), currentEvent.getRegistrationEnd());
        previewDaysLeft.setText(registrationText);
        // Handle geolocation status
        updateGeolocationStatus(currentEvent.getGeolocationRequired());

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

        // Generate QR Code for the Event
        QRGenerator qrGenerator = new QRGenerator();
        Bitmap qrBitmap = qrGenerator.generateQR("event:"+currentEvent.getEventID());

        if (qrBitmap != null) {
            qrCodeImage.setImageBitmap(qrBitmap);
            qrCodeImage.setVisibility(View.VISIBLE);
        } else {
            qrCodeImage.setVisibility(View.GONE); // hide if QR generation failed
        }

        // Load waitlist users
        eventUserLinkDB.getWaitListUsers(currentEvent.getLinkIDs(), new EventUserLinkDB.GetCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> waitListLinkIDs) {
                // Get Number of People in Wait List
                String numWaitList = waitListLinkIDs.size() + "";
                previewCurrentlyWaiting.setText(numWaitList);
                TextView geoHeading = findViewById(R.id.event_map_title);
                // Hide mapContainer if no one is waiting or if current event has no geolocation enabled
                if (waitListLinkIDs.isEmpty() || !currentEvent.getGeolocationRequired()) {
                    geoHeading.setVisibility(View.GONE);
                    mapTitle.setVisibility(View.GONE);
                    mapContainer.setVisibility(View.GONE);
                } else {
                    mapContainer.setVisibility(View.VISIBLE);
                    // Check if geolocation is enabled, only then show the map
                    if (currentEvent.getGeolocationRequired()) {
                        // At this point, geolocation required - KEEP MAP VISIBLE
                        mapContainer.setVisibility(View.VISIBLE);
                        mapTitle.setVisibility(View.VISIBLE);

                        // Create a SupportMapFragment dynamically
                        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.map_container, mapFragment)
                                .commit();

                        // When map is ready, plot the entrants
                        mapFragment.getMapAsync(googleMap -> {
                            plotEntrantsOnMap(googleMap, currentEvent.getLinkIDs(), mapContainer);
                        });

                    } else {
                        geoHeading.setVisibility(View.GONE);
                        mapContainer.setVisibility(View.GONE);
                    }
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
                    setupPostDrawLayout();
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
    @SuppressLint("SetTextI18n")
    private void setupPostDrawLayout() {
        // --- View bindings ---
        ImageView eventImage = findViewById(R.id.eventImage);
        TextView previewEventName = findViewById(R.id.previewEventName);
        TextView previewDescription = findViewById(R.id.previewDescription);
        TextView previewGuidelines = findViewById(R.id.previewGuidelines);
        TextView previewLocation = findViewById(R.id.previewLocation);
        TextView previewTimeAndDay = findViewById(R.id.previewTimeAndDay);
        TextView previewDateRange = findViewById(R.id.previewDateRange);
        TextView previewDuration = findViewById(R.id.previewDuration);
        TextView previewPrice = findViewById(R.id.previewPrice);
        TextView previewSpotsOpen = findViewById(R.id.previewSpotsOpen);

        LinearLayout sampledEntrantsContainer = findViewById(R.id.sampled_entrants_container);
        LinearLayout cancelledEntrantsContainer = findViewById(R.id.cancelled_entrants_container);
        LinearLayout allEntrantsContainer = findViewById(R.id.all_entrants_container);

        Button buttonFillSpots = findViewById(R.id.button_fillSpots);
        Button buttonConfirm = findViewById(R.id.button_confirm);
        Button buttonBack = findViewById(R.id.button_BackPostDraw);

        FrameLayout mapContainer = findViewById(R.id.map_container);
        if (currentEvent.getGeolocationRequired()) {
            mapContainer.setVisibility(View.VISIBLE);

            // Create a SupportMapFragment dynamically
            SupportMapFragment mapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.map_container, mapFragment)
                    .commit();

            // When map is ready, plot the entrants
            mapFragment.getMapAsync(googleMap -> {
                plotEntrantsOnMap(googleMap, currentEvent.getLinkIDs(), mapContainer);
            });

        } else {
            TextView geoHeading = findViewById(R.id.GeolocationHeading);
            geoHeading.setVisibility(View.GONE);
            mapContainer.setVisibility(View.GONE);
        }

        // --- Populate Event info ---
        previewEventName.setText(currentEvent.getName());
        previewDescription.setText(currentEvent.getDescription());
        previewGuidelines.setText(currentEvent.getGuidelines());
        previewLocation.setText(currentEvent.getLocation());
        previewTimeAndDay.setText(currentEvent.getTime());
        previewDateRange.setText(buildDateRange(currentEvent.getStartDate(), currentEvent.getEndDate()));
        previewDuration.setText(currentEvent.getDuration());
        String priceText = formatPrice(currentEvent.getPrice().toString());
        previewPrice.setText(priceText);
        String spotsOpen = (currentEvent.getCapacity() - currentEvent.getTotalWaitlist()) == 0
                ? "All spots are filled!"
                : Integer.toString(currentEvent.getCapacity() - currentEvent.getTotalWaitlist());
        previewSpotsOpen.setText(spotsOpen);

        // Populate sampled entrants
        if (currentEvent.getSampledIDs().isEmpty()) {
            buttonConfirm.setEnabled(false);
        }

        populateSampledEntrants(sampledEntrantsContainer, currentEvent.getSampledIDs());
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

    private void populateSampledEntrants(LinearLayout container, List<String> linkIDs) {
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
                            .inflate(R.layout.sampled_user_blob, container, false);
                    ((TextView) blobView.findViewById(R.id.profileNameTextView)).setText(profile.getName());
                    ((ImageView) blobView.findViewById(R.id.profileIcon)).setImageResource(R.drawable.ic_profile);
                    ((ImageView) blobView.findViewById(R.id.cancelButton)).setImageResource(R.drawable.ic_cross);
                    container.addView(blobView);
                    // Set an OnClickListener for the cancel button
                    blobView.findViewById(R.id.cancelButton).setOnClickListener(v -> {
                        eventHandler.cancelUser(currentEvent, Integer.parseInt(userID), new ProfileDB.GetCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer result) {
                                Toast.makeText(OrganizerEventActivity.this, "User cancelled successfully", Toast.LENGTH_SHORT).show();
                                // Refresh the layout to reflect changes
                                setContentView(R.layout.section2_organizereventview_postdraw);

                                setupPostDrawLayout();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(OrganizerEventActivity.this, "Error cancelling user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(OrganizerEventActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private String buildDateRange(String startDate, String endDate) {
        if (startDate != null && startDate.equals(endDate)) {
            return startDate;
        } else if (startDate != null && endDate != null && !endDate.isEmpty()) {
            return startDate + " to " + endDate;
        } else if (startDate != null) {
            return startDate;
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
        if (registrationStart != null && registrationStart.equals(registrationEnd)) {
            return registrationStart;
        } else if (registrationStart != null && registrationEnd != null && !registrationEnd.isEmpty()) {
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

    private void plotEntrantsOnMap(GoogleMap googleMap, List<String> linkIDs, FrameLayout mapContainer) {

        if (linkIDs == null || linkIDs.isEmpty()) {
            mapContainer.setVisibility(View.GONE);
            return;
        }

        int organizerID = getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("userID", -1);
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        List<LatLng> allPositions = new ArrayList<>();

        final int total = linkIDs.size();
        final int[] completed = {0};

        for (String linkID : linkIDs) {

            String userIDStr = linkID.split("_")[1];
            if (Integer.parseInt(userIDStr) == organizerID) {
                completed[0]++;
                continue;
            }

            eventUserLinkDB.getEventUserLinkByID(linkID, new EventUserLinkDB.GetCallback<EventUserLink>() {
                @Override
                public void onSuccess(EventUserLink link) {
                    if (link != null && link.getLatitude() != null && link.getLongitude() != null) {
                        LatLng pos = new LatLng(link.getLatitude(), link.getLongitude());
                        googleMap.addMarker(new MarkerOptions().position(pos));
                        allPositions.add(pos);
                    }

                    completed[0]++;
                    checkIfDone();
                }

                @Override
                public void onFailure(Exception e) {
                    completed[0]++;
                    checkIfDone();
                }

                private void checkIfDone() {
                    if (completed[0] == total) {
                        if (allPositions.isEmpty()) {
                            mapContainer.setVisibility(View.GONE);
                            return;
                        }
                        for (LatLng p : allPositions) boundsBuilder.include(p);

                        LatLngBounds bounds = boundsBuilder.build();
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                    }
                }
            });
        }
    }
}