package com.example.hotpot0.section3.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.EventDB;
import com.example.hotpot0.models.EventUserLinkDB;
import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.models.UserProfile;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class AdminSearchActivity extends AppCompatActivity {

    private EventDB eventDB;
    private ProfileDB profileDB;

    private ListView listView;
    private EditText searchBar;
    private ChipGroup chipGroup;

    // EVENTS
    private final List<Event> allEvents = new ArrayList<>();
    private final List<Event> filteredEvents = new ArrayList<>();

    // ORGANIZERS (REAL USERS)
    private final List<UserProfile> allOrganizers = new ArrayList<>();
    private final List<UserProfile> filteredOrganizers = new ArrayList<>();

    private AdminEventAdapter eventAdapter;
    private OrganizerListAdapter organizerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section3_adminsearch_activity);

        eventDB = new EventDB();
        profileDB = new ProfileDB();

        listView = findViewById(R.id.searchResultsListView);
        searchBar = findViewById(R.id.searchEditText);
        chipGroup = findViewById(R.id.filterChipGroup);

        chipGroup.check(R.id.eventsChip);

        loadEvents();
        loadOrganizers();

        setupSearchFilter();
        setupChipSwitching();
        setupItemClick();
    }

    // ------------------------ LOAD EVENTS ------------------------
    private void loadEvents() {
        eventDB.getAllEvents(new EventDB.GetCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> events) {
                allEvents.clear();
                allEvents.addAll(events);

                filteredEvents.clear();
                filteredEvents.addAll(allEvents);

                eventAdapter = new AdminEventAdapter(AdminSearchActivity.this, filteredEvents);

                // Default tab = events
                listView.setAdapter(eventAdapter);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminSearchActivity.this,
                        "Failed loading events.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ------------------------ LOAD ORGANIZERS ------------------------
    private void loadOrganizers() {

        EventUserLinkDB linkDB = new EventUserLinkDB();

        // Step 1: Get all userIDs where status == "Organizer"
        linkDB.getAllOrganizers(new EventUserLinkDB.GetCallback<List<Integer>>() {
            @Override
            public void onSuccess(List<Integer> organizerIDs) {

                if (organizerIDs.isEmpty()) {
                    filteredOrganizers.clear();
                    return;
                }

                // Step 2: Load all UserProfiles and match with organizer IDs
                profileDB.getAllUsers(new ProfileDB.GetCallback<List<UserProfile>>() {
                    @Override
                    public void onSuccess(List<UserProfile> users) {

                        allOrganizers.clear();

                        for (UserProfile u : users) {
                            if (organizerIDs.contains(u.getUserID())) {
                                allOrganizers.add(u);
                            }
                        }

                        filteredOrganizers.clear();
                        filteredOrganizers.addAll(allOrganizers);

                        organizerAdapter =
                                new OrganizerListAdapter(AdminSearchActivity.this, filteredOrganizers);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(AdminSearchActivity.this,
                                "Failed loading organizer profiles.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminSearchActivity.this,
                        "Failed loading organizer links.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ------------------------ SEARCH BAR ------------------------
    private void setupSearchFilter() {

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String q = s.toString().toLowerCase().trim();

                int selected = chipGroup.getCheckedChipId();

                // EVENTS
                if (selected == R.id.eventsChip) {
                    filteredEvents.clear();

                    for (Event e : allEvents) {
                        if (e.getName() != null &&
                                e.getName().toLowerCase().contains(q)) {
                            filteredEvents.add(e);
                        }
                    }

                    if (eventAdapter != null) eventAdapter.notifyDataSetChanged();
                }

                // ORGANIZERS
                else if (selected == R.id.organizersChip) {
                    filteredOrganizers.clear();

                    for (UserProfile u : allOrganizers) {
                        String name = u.getName() != null ? u.getName().toLowerCase() : "";
                        String id = String.valueOf(u.getUserID());

                        if (name.contains(q) || id.contains(q)) {
                            filteredOrganizers.add(u);
                        }
                    }

                    if (organizerAdapter != null) organizerAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    // ------------------------ CHIP SWITCHING ------------------------
    private void setupChipSwitching() {

        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {

            if (checkedId == R.id.eventsChip) {
                if (eventAdapter != null) listView.setAdapter(eventAdapter);
            }

            else if (checkedId == R.id.organizersChip) {
                if (organizerAdapter != null) listView.setAdapter(organizerAdapter);
            }

            else if (checkedId == R.id.profilesChip) {
                Toast.makeText(this,
                        "Profiles search not implemented.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ------------------------ ITEM CLICK ------------------------
    private void setupItemClick() {

        listView.setOnItemClickListener((parent, view, position, id) -> {

            int selected = chipGroup.getCheckedChipId();

            // EVENTS → ManageEventActivity
            if (selected == R.id.eventsChip) {
                if (position >= filteredEvents.size()) return;

                Event e = filteredEvents.get(position);

                Intent i = new Intent(this, ManageEventActivity.class);
                i.putExtra("eventID", e.getEventID());
                startActivity(i);
            }

            // ORGANIZERS → AdminOrganizerViewActivity
            else if (selected == R.id.organizersChip) {
                if (position >= filteredOrganizers.size()) return;

                UserProfile organizer = filteredOrganizers.get(position);

                Intent i = new Intent(this, AdminOrganizerViewActivity.class);
                i.putExtra("organizerID", organizer.getUserID());
                startActivity(i);
            }
        });
    }
}
