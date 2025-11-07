package com.example.hotpot0.section2.views;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.EventDB;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * This view shows the Search tab where the user can search for a specific
 * event by name or QR code. The user can also apply filters to refine their search.
 */
public class SearchActivity extends AppCompatActivity {
    private ListView listView;
    private SearchView searchView;
    private ImageButton filterButton, qrButton, infoButton;
    private BottomNavigationView bottomNav;
    private ArrayAdapter<String> adapter;
    private List<String> eventList;
    private List<String> filteredList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section2_searchevents_activity);

        // Initialize UI elements
        listView = findViewById(R.id.event_list_view);
        searchView = findViewById(R.id.searchView);
        filterButton = findViewById(R.id.filter_button);
        qrButton = findViewById(R.id.scan_qr_button);
        infoButton = findViewById(R.id.info_button);
        bottomNav = findViewById(R.id.bottomNavigationView);

        eventList = new ArrayList<>();
        filteredList = new ArrayList<>(eventList);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filteredList);
        listView.setAdapter(adapter);

        // Get all events from database
        EventDB eventDB = new EventDB();
        eventDB.getAllEvents(new EventDB.GetCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> events) {
                eventList.clear();
                for (Event e : events) {
                    eventList.add(e.getName());
                }
                filteredList.clear();
                filteredList.addAll(eventList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(SearchActivity.this, "Failed to load events.", Toast.LENGTH_SHORT).show();
            }
        });

        // SearchView filtering
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterList(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });

        // Filter Button
        filterButton.setOnClickListener(view -> filterDialog());

        // QR Button
        qrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(SearchActivity.this, Section3QrCodeSearchActivity.class);
//                startActivity(intent);
//                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                Toast.makeText(SearchActivity.this, "Open QR Scanner", Toast.LENGTH_SHORT).show();

            }
        });

        // Info Button
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SearchActivity.this, "Open Information Dialog", Toast.LENGTH_SHORT).show();

            }
        });

        // Bottom Navigation
        bottomNav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    Intent intent = new Intent(SearchActivity.this, HomeActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                } else if (id == R.id.nav_profile) {
                    Intent intent = new Intent(SearchActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                } else if (id == R.id.nav_notifications) {
                    Intent intent = new Intent(SearchActivity.this, NotificationsActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                } else if (id == R.id.nav_events) {
                    Intent intent = new Intent(SearchActivity.this, CreateEventActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                } else if (id == R.id.nav_search) {
                    return true;
                }

                return false;
            }
        });
    }

    private void filterList(String query) {
        filteredList.clear();
        if (query == null || query.isEmpty()) {
            filteredList.addAll(eventList);
        } else {
            for (String event : eventList) {
                if (event.toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(event);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    // Opening filer dialog box
    private void filterDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.section2_filterdialogbox_activity, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();

        // Back button inside the dialog
        Button backButton = dialogView.findViewById(R.id.button_BackSearchEvents);
        backButton.setOnClickListener(v -> {
                    dialog.dismiss();
                });

        // Handling a filter action
        CheckBox filter1 = dialogView.findViewById(R.id.checkBox4);
        filter1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(this, "CheckBox 1 selected", Toast.LENGTH_SHORT).show();
            }
        });
    }
}



