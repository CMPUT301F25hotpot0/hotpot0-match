package com.example.hotpot0.section2.views;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.EventDB;
import com.example.hotpot0.models.EventUserLink;
import com.example.hotpot0.models.EventUserLinkDB;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Activity that allows users to search and view active events.
 * <p>
 * Users can search for events by name, view their status in each event,
 * and navigate between different sections of the app using the bottom navigation.
 * The activity also provides a help/info dialog explaining the app's functionality.
 * </p>
 */
public class SearchActivity extends AppCompatActivity {

    private int userID;
    private BottomNavigationView bottomNav;
    private ListView eventListView;
    private TextInputEditText searchEditText;
    private EventDB eventDB;
    private EventUserLinkDB eventUserLinkDB = new EventUserLinkDB();
    private ImageButton infoButton;
    private ImageButton scanQrButton;
    private Chip regFilterChip;
    private Chip fromDateFilterChip;
    private Chip toDateFilterChip;

    private Date fromDateFilter = null;
    private Date toDateFilter = null;
    private final SimpleDateFormat EVENT_DATE_FORMATTER = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    private enum RegStatus { ALL, ACTIVE, OPENING_SOON }
    private RegStatus regStatusFilter = RegStatus.ALL;

    /**
     * Initializes the activity, sets up UI elements, bottom navigation,
     * search functionality, and loads all active events.
     *
     * @param savedInstanceState previously saved state (if any)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section2_searchevents_activity);

        // Initialize UI elements
        eventListView = findViewById(R.id.event_list_view);
        searchEditText = findViewById(R.id.searchEditText);
        bottomNav = findViewById(R.id.bottomNavigationView);
        infoButton = findViewById(R.id.info_button);
        scanQrButton = findViewById(R.id.scan_qr_button);

        // Initialize filter chips
        regFilterChip = findViewById(R.id.regFilterChip);
        fromDateFilterChip = findViewById(R.id.fromDateFilterChip);
        toDateFilterChip = findViewById(R.id.toDateFilterChip);

        userID = getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("userID", -1);

        // Handling Bottom Navigation Bar
        bottomNav = findViewById(R.id.bottomNavigationView);
        setupBottomNavigation();

        // Info button functionality
        infoButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Welcome to Eventure!")
                    .setMessage("How it Works:\n\n" +
                            "• Use the bottom toolbar to navigate between pages.\n\n" +
                            "• The Home page shows your confirmed events, pending events, and past events.\n\n" +
                            "• Confirmed events are events that you have been selected for and you have accepted.\n\n" +
                            "• Pending events are events that you are in waitlist for. You will receive a notification about the decision. " +
                            "You will then be able to accept or decline the invitation. You are also able to leave the waitlist.\n\n" +
                            "• Past events show your event history and their outcomes.\n\n" +
                            "• If you are an organizer, use the Create Event option to create an event. You will be able pick the entrants and related options.\n\n" +
                            "• Use the Search option to search for events. You can use the filter option to filter it to your interests. \n\n" +
                            "• Entrants are randomly sampled to join the event, in a completely fair manner.\n\n" +
                            "• Accept or Decline your invite if you are sampled.\n\n" +
                            "• Even if you're not sampled, you can rely on your luck to get sampled again if another user declines.\n\n" +
                            "• Notifications tab show the notifications you have received. This includes invitations and results. \n\n" +
                            "• View your profile in the Profile Tab. ")
                    .setPositiveButton("Ok", null)
                    .show();
        });

        scanQrButton.setOnClickListener(v -> {
            // Toast.makeText(this, "Launching Camera", Toast.LENGTH_SHORT).show();
            // Launch QRActivity
            Intent intent = new Intent(SearchActivity.this, QRActivity.class);
            startActivity(intent);
        });

        // Registration Status Filter Chip
        regFilterChip.setText("All"); // Default
        regFilterChip.setText("All"); // Default
        regFilterChip.setOnClickListener(v -> {
            // Cycle through enum
            switch (regStatusFilter) {
                case ALL:
                    regStatusFilter = RegStatus.ACTIVE;
                    regFilterChip.setText("Reg. Active");
                    break;
                case ACTIVE:
                    regStatusFilter = RegStatus.OPENING_SOON;
                    regFilterChip.setText("Reg. Soon");
                    break;
                case OPENING_SOON:
                    regStatusFilter = RegStatus.ALL;
                    regFilterChip.setText("All");
                    break;
            }
            applyCombinedFilters();
        });

        // Date Filter Chips
        fromDateFilterChip.setOnClickListener(v -> {
            showDatePicker(true);
        });

        toDateFilterChip.setOnClickListener(v -> {
            showDatePicker(false);
        });

        // Long Press to clear chips
        fromDateFilterChip.setOnLongClickListener(v -> {
            fromDateFilter = null;
            fromDateFilterChip.setText("From");
            applyCombinedFilters();
            return true; // indicate the event is consumed
        });

        toDateFilterChip.setOnLongClickListener(v -> {
            toDateFilter = null;
            toDateFilterChip.setText("To");
            applyCombinedFilters();
            return true;
        });

        eventDB = new EventDB();
        // Info Button
        infoButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("How the app works!")
                    .setMessage("• Use the bottom toolbar to navigate between pages.\n\n" +
                            "• The Home page shows your confirmed events, pending events, and past events.\n\n" +
                            "• Confirmed events are events that you have been selected for and you have accepted.\n\n" +
                            "• Pending events are events that you are in waitlist for. You will receive a notification about the decision. " +
                            "You will then be able to accept or decline the invitation. You are also able to leave the waitlist.\n\n" +
                            "• Past events show your event history and their outcomes.\n\n" +
                            "• If you are an organizer, use the Create Event option to create an event. You will be able pick the entrants and related options.\n\n" +
                            "• Use the Search option to search for events. You can use the filter option to filter it to your interests. \n\n" +
                            "• Notifications tab show the notifications you have received. This includes invitations and results. \n\n" +
                            "• View your profile in the Profile Tab. ")
                    .setPositiveButton("Ok", null)
                    .show();
        });

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
        searchEditText.setOnClickListener(v -> {
            // Clear the hint text when user clicks to type
            if (searchEditText.getText().toString().isEmpty()) {
                searchEditText.setHint("");
            }
        });

        // Add text watcher for real-time search
        searchEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // filterEvents(s.toString());
                applyCombinedFilters();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    /**
     * Loads all active events from the database, fetches the user's status for each event,
     * and updates the {@link ListView} with an {@link EventBlobAdapter}.
     */
    public void loadAllEvents() {
        eventDB.getAllActiveEvents(new EventDB.GetCallback<List<Event>>() {
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
                                    fadeIn(eventListView);
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

    /**
     * Filters events based on the search query and updates the {@link ListView} with results.
     *
     * @param query the search query entered by the user
     */
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
                    fadeIn(eventListView);
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(SearchActivity.this, "Error filtering events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fadeIn(View view) {
        view.setAlpha(0f);
        view.animate().alpha(1f).setDuration(300).start();
    }

    private void setupBottomNavigation() {
        // Highlight the Search tab when entering this activity
        bottomNav.setSelectedItemId(R.id.nav_search);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(SearchActivity.this, HomeActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            }

            if (id == R.id.nav_events) {
                startActivity(new Intent(SearchActivity.this, CreateEventActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            }

            if (id == R.id.nav_search) {
                return true;
            }

            if (id == R.id.nav_notifications) {
                startActivity(new Intent(SearchActivity.this, NotificationsActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            }

            if (id == R.id.nav_profile) {
                startActivity(new Intent(SearchActivity.this, ProfileActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            }

            return false;
        });
    }

    // Date Filtering

    private void showDatePicker(boolean isFromDate) {

        final Calendar calendar = Calendar.getInstance();
        Date preselected = isFromDate ? fromDateFilter : toDateFilter;

        if (preselected != null) {
            calendar.setTime(preselected);
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedCal = Calendar.getInstance();
                    selectedCal.set(selectedYear, selectedMonth, selectedDay, 0, 0, 0);
                    Date selectedDate = selectedCal.getTime();

                    if (isFromDate) {
                        fromDateFilter = selectedDate;
                        fromDateFilterChip.setText("From: " + EVENT_DATE_FORMATTER.format(selectedDate));
                    } else {
                        toDateFilter = selectedDate;
                        toDateFilterChip.setText("To: " + EVENT_DATE_FORMATTER.format(selectedDate));
                    }

                    applyCombinedFilters();
                },
                year, month, day
        );

        datePicker.show();
    }

    /**
     * Filters a list of events based on the selected date range.
     *
     * @param events the list of events to filter
     * @return the filtered list of events
     */
    private List<Event> filterByDateRange(List<Event> events) {
        List<Event> result = new ArrayList<>();

        for (Event event : events) {
            Date eventStart = parseEventDate(event.getStartDate());
            Date eventEnd = parseEventDate(event.getEndDate());

            if (eventStart == null) continue;
            if (eventEnd == null) eventEnd = eventStart;

            boolean matches = true;

            if (fromDateFilter != null) {
                matches &= !eventStart.before(fromDateFilter); // Event starts on/after From
            }

            if (toDateFilter != null) {
                matches &= !eventEnd.after(toDateFilter);     // Event ends on/before To
            }

            if (matches) result.add(event);
        }

        return result;
    }

    /**
     * Applies all active filters (search query, registration status, date range)
     * to the list of events and updates the ListView with the filtered results.
     */
    private void applyCombinedFilters() {
        String query = searchEditText.getText().toString().toLowerCase();

        eventDB.getAllActiveEvents(new EventDB.GetCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> allEvents) {

                List<Event> filtered = new ArrayList<>();

                for (Event event : allEvents) {
                    // Filter by name
                    String queryLower = query.toLowerCase();

                    if (!event.getName().toLowerCase().contains(queryLower) &&
                            !event.getDescription().toLowerCase().contains(queryLower)) {
                        continue; // skip if neither matches
                    }

                    // Filter by registration status
                    if (!matchesRegStatus(event)) continue;

                    filtered.add(event);
                }

                filtered = filterByDateRange(filtered);

                List<String> statuses = new ArrayList<>();
                for (int i = 0; i < filtered.size(); i++) statuses.add("");

                List<Event> finalFiltered = filtered;
                runOnUiThread(() -> {
                    eventListView.setAdapter(new EventBlobAdapter(SearchActivity.this, finalFiltered, statuses, userID));
                    fadeIn(eventListView);
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(SearchActivity.this, "Failed to apply filters", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Parses a date string into a Date object.
     *
     * @param dateString the date string to parse
     * @return the parsed Date object, or null if parsing fails
     */
    private Date parseEventDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) return null;

        try {
            return EVENT_DATE_FORMATTER.parse(dateString);
        } catch (Exception e) {
            return null;
        }
    }

    /** Registration Status Filter
     *
     * @param dateString
     */
    private Date parseRegDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) return null;
        try {
            return EVENT_DATE_FORMATTER.parse(dateString);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks if an event matches the selected registration status filter.
     *
     * @param event the event to check
     * @return true if the event matches the filter, false otherwise
     */
    private boolean matchesRegStatus(Event event) {
        if (!Boolean.TRUE.equals(event.getIsEventActive())) return false; // Only active events

        Date now = new Date();
        Date regStart = parseRegDate(event.getRegistrationStart());
        Date regEnd = parseRegDate(event.getRegistrationEnd());

        switch (regStatusFilter) {
            case ALL:
                return true;
            case ACTIVE:
                if (regStart != null && regEnd != null) {
                    return !now.before(regStart) && !now.after(regEnd);
                }
                return false;
            case OPENING_SOON:
                if (regStart != null) {
                    return now.before(regStart);
                }
                return false;
        }
        return true;
    }

}