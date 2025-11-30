package com.example.hotpot0.section3.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.EventDB;
import com.example.hotpot0.section3.adapters.AdminEventAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class AdminSearchActivity extends AppCompatActivity {

    private EventDB eventDB;
    private ListView listView;
    private EditText searchBar;
    private ChipGroup chipGroup;

    private List<Event> allEvents = new ArrayList<>();
    private List<Event> filteredEvents = new ArrayList<>();

    private AdminEventAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section3_adminsearch_activity);

        eventDB = new EventDB();

        listView = findViewById(R.id.searchResultsListView);
        searchBar = findViewById(R.id.searchEditText);
        chipGroup = findViewById(R.id.filterChipGroup);

        loadEvents();
        setupSearchFilter();
        setupItemClick();

        // Set Home as selected by default
        BottomNavigationView bottomNav = findViewById(R.id.adminBottomNavigationView);

        // Optional: highlight "Home" as selected initially
        bottomNav.setSelectedItemId(R.id.admin_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.admin_search) {
                // Already on search
                return true;
            } else if (id == R.id.admin_home) {
                Intent searchIntent = new Intent(AdminSearchActivity.this, AdminHomeActivity.class);
                startActivity(searchIntent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                return true;
            } else if (id == R.id.admin_images) {
                startActivity(new Intent(AdminSearchActivity.this, AdminImageActivity.class));
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                return true;
            } else if (id == R.id.admin_settings) {
//                startActivity(new Intent(AdminSearchActivity.this, AdminSettingsActivity.class));
//                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                return true;
            }
            return false;
        });
    }

    private void loadEvents() {
        eventDB.getAllEvents(new EventDB.GetCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> events) {
                allEvents = events;
                filteredEvents = new ArrayList<>(events);

                adapter = new AdminEventAdapter(AdminSearchActivity.this, filteredEvents);
                listView.setAdapter(adapter);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminSearchActivity.this, "Failed: " + e, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearchFilter() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim().toLowerCase();
                filteredEvents.clear();

                for (Event event : allEvents) {
                    if (event.getName().toLowerCase().contains(query)) {
                        filteredEvents.add(event);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void setupItemClick() {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Event selected = filteredEvents.get(position);

            Intent intent = new Intent(AdminSearchActivity.this, ManageEventActivity.class);
            intent.putExtra("eventID", selected.getEventID());
            startActivity(intent);
        });
    }
}

