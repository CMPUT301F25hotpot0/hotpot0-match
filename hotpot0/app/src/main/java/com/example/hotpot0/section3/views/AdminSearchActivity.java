package com.example.hotpot0.section3.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.EventDB;
import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.models.UserProfile;
import com.example.hotpot0.section3.adapters.AdminEventAdapter;
import com.example.hotpot0.section3.views.OrganizerListAdapter;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class AdminSearchActivity extends AppCompatActivity {

    private EventDB eventDB;
    private ProfileDB profileDB;

    private ListView listView;
    private EditText searchBar;
    private ChipGroup chipGroup;

    // Events
    private List<Event> allEvents = new ArrayList<>();
    private List<Event> filteredEvents = new ArrayList<>();

    // Organizers
    private List<UserProfile> allOrganizers = new ArrayList<>();
    private List<UserProfile> filteredOrganizers = new ArrayList<>();

    private AdminEventAdapter adapter;
    private OrganizerListAdapter orgAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section3_adminsearch_activity);

        eventDB = new EventDB();
        profileDB = new ProfileDB();

        listView = findViewById(R.id.searchResultsListView);
        searchBar = findViewById(R.id.searchEditText);
        chipGroup = findViewById(R.id.filterChipGroup);

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
                allEvents = events;
                filteredEvents = new ArrayList<>(events);

                adapter = new AdminEventAdapter(AdminSearchActivity.this, filteredEvents);
                listView.setAdapter(adapter); // default view
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminSearchActivity.this, "Failed: " + e, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ------------------------ LOAD ORGANIZERS ------------------------
    private void loadOrganizers() {

        profileDB.getAllUserProfiles(new ProfileDB.GetCallback<List<UserProfile>>() {
            @Override
            public void onSuccess(List<UserProfile> users) {
                allOrganizers.clear();

                for (UserProfile u : users) {

                    if (u.getLinkIDs() != null) {
                        for (String link : u.getLinkIDs()) {
                            // You told me "Status = Organizer" is stored inside linkIDs
                            if (link.contains("org")) {
                                allOrganizers.add(u);
                                break;
                            }
                        }
                    }
                }

                filteredOrganizers = new ArrayList<>(allOrganizers);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminSearchActivity.this, "Failed to load organizers", Toast.LENGTH_SHORT).show();
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
                String q = s.toString().trim().toLowerCase();

                int selected = chipGroup.getCheckedChipId();

                if (selected == R.id.eventsChip) {
                    filteredEvents.clear();
                    for (Event e : allEvents) {
                        if (e.getName().toLowerCase().contains(q)) {
                            filteredEvents.add(e);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }

                else if (selected == R.id.organizersChip) {
                    filteredOrganizers.clear();

                    for (UserProfile u : allOrganizers) {
                        if (u.getName().toLowerCase().contains(q)) {
                            filteredOrganizers.add(u);
                        }
                    }
                    orgAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    // ------------------------ CHIP SWITCHING ------------------------
    private void setupChipSwitching() {
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {

            if (checkedIds.isEmpty()) return;

            int selected = checkedIds.get(0);

            if (selected == R.id.eventsChip) {
                adapter = new AdminEventAdapter(AdminSearchActivity.this, filteredEvents);
                listView.setAdapter(adapter);
            }

            else if (selected == R.id.organizersChip) {
                orgAdapter = new OrganizerListAdapter(AdminSearchActivity.this, filteredOrganizers);
                listView.setAdapter(orgAdapter);
            }
        });
    }

    // ------------------------ ITEM CLICK ------------------------
    private void setupItemClick() {
        listView.setOnItemClickListener((parent, view, position, id) -> {

            int selected = chipGroup.getCheckedChipId();

            if (selected == R.id.eventsChip) {
                Event chosen = filteredEvents.get(position);

                Intent i = new Intent(AdminSearchActivity.this, ManageEventActivity.class);
                i.putExtra("eventID", chosen.getEventID());
                startActivity(i);
            }

            else if (selected == R.id.organizersChip) {
                UserProfile chosen = filteredOrganizers.get(position);

                Intent i = new Intent(AdminSearchActivity.this, AdminOrganizerViewActivity.class);
                i.putExtra("organizerID", chosen.getUserID());
                startActivity(i);
            }
        });
    }
}
