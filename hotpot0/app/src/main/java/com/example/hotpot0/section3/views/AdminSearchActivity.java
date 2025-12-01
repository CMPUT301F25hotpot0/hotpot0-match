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
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class AdminSearchActivity extends AppCompatActivity {

    private EventDB eventDB;

    private ListView listView;
    private EditText searchBar;
    private ChipGroup chipGroup;

    // Events (for "Events" chip)
    private List<Event> allEvents = new ArrayList<>();
    private List<Event> filteredEvents = new ArrayList<>();

    // Events (for "Organizers" chip – same events, different meaning)
    private List<Event> allOrganizerEvents = new ArrayList<>();
    private List<Event> filteredOrganizerEvents = new ArrayList<>();

    private AdminEventAdapter adapter;              // for Events tab
    private OrganizerEventAdapter organizerAdapter; // for Organizers tab

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section3_adminsearch_activity);

        eventDB = new EventDB();

        listView = findViewById(R.id.searchResultsListView);
        searchBar = findViewById(R.id.searchEditText);
        chipGroup = findViewById(R.id.filterChipGroup);

        // Ensure a default selection
        chipGroup.check(R.id.eventsChip);

        loadEvents();
        setupSearchFilter();
        setupChipSwitching();
        setupItemClick();
    }

    // ------------------------ LOAD EVENTS ------------------------
    private void loadEvents() {
        eventDB.getAllEvents(new EventDB.GetCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> events) {
                // All events for normal "Events" tab
                allEvents.clear();
                allEvents.addAll(events);
                filteredEvents = new ArrayList<>(allEvents);

                // Prepare list for "Organizers" tab (only events that have an organizerID)
                allOrganizerEvents.clear();
                for (Event e : events) {
                    if (e.getOrganizerID() != null) {
                        allOrganizerEvents.add(e);
                    }
                }
                filteredOrganizerEvents = new ArrayList<>(allOrganizerEvents);

                // Default adapter = Events view
                adapter = new AdminEventAdapter(AdminSearchActivity.this, filteredEvents);
                listView.setAdapter(adapter);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(
                        AdminSearchActivity.this,
                        "Failed loading events: " + e.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
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
                if (selected == View.NO_ID) selected = R.id.eventsChip;

                // Filter EVENTS tab
                if (selected == R.id.eventsChip) {
                    filteredEvents.clear();
                    for (Event e : allEvents) {
                        if (e.getName() != null && e.getName().toLowerCase().contains(q)) {
                            filteredEvents.add(e);
                        }
                    }
                    if (adapter != null) adapter.notifyDataSetChanged();
                }

                // Filter ORGANIZERS tab
                else if (selected == R.id.organizersChip) {
                    filteredOrganizerEvents.clear();
                    for (Event e : allOrganizerEvents) {
                        if (e.getName() != null && e.getName().toLowerCase().contains(q)) {
                            filteredOrganizerEvents.add(e);
                        }
                    }
                    if (organizerAdapter != null) organizerAdapter.notifyDataSetChanged();
                }

                // Profiles search not implemented
            }
        });
    }

    // ------------------------ CHIP SWITCHING ------------------------
    private void setupChipSwitching() {
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {

            if (checkedId == R.id.eventsChip) {
                if (adapter == null) {
                    adapter = new AdminEventAdapter(AdminSearchActivity.this, filteredEvents);
                }
                listView.setAdapter(adapter);
            }

            // FIX #1 — this must run BEFORE profilesChip
            else if (checkedId == R.id.organizersChip) {
                if (organizerAdapter == null) {
                    organizerAdapter = new OrganizerEventAdapter(AdminSearchActivity.this, filteredOrganizerEvents);
                }
                listView.setAdapter(organizerAdapter);
            }

            // FIX #2 — this must be last
            else if (checkedId == R.id.profilesChip) {
                Toast.makeText(
                        AdminSearchActivity.this,
                        "Profiles search not implemented yet.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    // ------------------------ ITEM CLICK ------------------------
    private void setupItemClick() {
        listView.setOnItemClickListener((parent, view, position, id) -> {

            int selected = chipGroup.getCheckedChipId();
            if (selected == View.NO_ID) selected = R.id.eventsChip;

            // EVENTS → ManageEventActivity
            if (selected == R.id.eventsChip) {
                if (position < 0 || position >= filteredEvents.size()) return;
                Event chosen = filteredEvents.get(position);

                Intent i = new Intent(AdminSearchActivity.this, ManageEventActivity.class);
                i.putExtra("eventID", chosen.getEventID());
                startActivity(i);
            }

            // ORGANIZERS → AdminOrganizerViewActivity
            else if (selected == R.id.organizersChip) {
                if (position < 0 || position >= filteredOrganizerEvents.size()) return;

                Event chosen = filteredOrganizerEvents.get(position);
                Integer organizerID = chosen.getOrganizerID();

                if (organizerID == null) {
                    Toast.makeText(this, "This event has no organizer linked.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent i = new Intent(AdminSearchActivity.this, AdminOrganizerViewActivity.class);
                i.putExtra("organizerID", organizerID);
                startActivity(i);
            }
        });
    }
}
