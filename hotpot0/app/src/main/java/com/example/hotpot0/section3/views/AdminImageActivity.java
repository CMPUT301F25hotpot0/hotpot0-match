package com.example.hotpot0.section3.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.EventDB;
import com.example.hotpot0.models.PicturesDB;
import com.example.hotpot0.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminImageActivity extends AppCompatActivity {

    private PicturesDB picturesDB;
    private EventDB eventDB = new EventDB();
    private RecyclerView recyclerView;
    private EditText searchEditText;
    private ArrayList<String> allImageUrls = new ArrayList<>();
    private ArrayList<String> filteredImageUrls = new ArrayList<>();
    private Map<Integer, String> eventNames = new HashMap<>();
    private AdminImageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section3_adminsearchimage_activity);

        picturesDB = new PicturesDB();
        recyclerView = findViewById(R.id.imagesRecyclerView);
        searchEditText = findViewById(R.id.search_edit_text);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminImageAdapter(
                this,
                filteredImageUrls,
                this::deleteImage,
                url -> {
                    Intent intent = new Intent(this, ManageImageActivity.class);
                    intent.putExtra("image_url", url);
                    startActivity(intent);
                },
                eventNames,
                this::extractEventId
        );

        recyclerView.setAdapter(adapter);

        loadImages();
        setupSearch();
        setupBottomNavigation();
    }

    /** Load all images from Firebase and fetch event names */
    private void loadImages() {
        picturesDB.getAllEventImages(new PicturesDB.Callback<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                allImageUrls.clear();
                allImageUrls.addAll(result);
                filteredImageUrls.clear();
                filteredImageUrls.addAll(allImageUrls);

                // Collect unique event IDs
                List<Integer> eventIds = new ArrayList<>();
                for (String url : allImageUrls) {
                    int eventId = extractEventId(url);
                    if (eventId != -1 && !eventIds.contains(eventId)) {
                        eventIds.add(eventId);
                    }
                }

                if (eventIds.isEmpty()) {
                    adapter.notifyDataSetChanged();
                    return;
                }

                // Fetch all event names in one go
                final int[] loadedCount = {0};
                for (int eventId : eventIds) {
                    eventDB.getEventByID(eventId, new EventDB.GetCallback<Event>() {
                        @Override
                        public void onSuccess(Event event) {
                            if (event == null) {
                                Log.e("AdminImageActivity", "Event not found for ID: " + eventId);
                                eventNames.put(eventId, "Unknown Event");
                            } else {
                                eventNames.put(eventId, event.getName());
                            }
                            loadedCount[0]++;
                            if (loadedCount[0] == eventIds.size()) runOnUiThread(() -> adapter.notifyDataSetChanged());
                        }



                        @Override
                        public void onFailure(Exception e) {
                            eventNames.put(eventId, "Unknown Event");
                            loadedCount[0]++;
                            if (loadedCount[0] == eventIds.size()) {
                                runOnUiThread(() -> adapter.notifyDataSetChanged());
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminImageActivity.this, "Failed to load images: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    /** Setup search functionality */
    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().toLowerCase();
                filteredImageUrls.clear();

                for (String url : allImageUrls) {
                    int eventId = extractEventId(url);
                    String eventName = eventNames.get(eventId);

                    if ((eventName != null && eventName.toLowerCase().contains(query))
                            || url.toLowerCase().contains(query)) {
                        filteredImageUrls.add(url);
                    }
                }

                adapter.notifyDataSetChanged();
            }
        });
    }

    /** Setup bottom navigation */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.adminBottomNavigationView);
        bottomNav.setSelectedItemId(R.id.admin_images);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.admin_images) return true;

            Intent intent = null;
            if (id == R.id.admin_search) intent = new Intent(this, AdminSearchActivity.class);
            else if (id == R.id.admin_home) intent = new Intent(this, AdminHomeActivity.class);
            else if (id == R.id.admin_settings) intent = new Intent(this, AdminSettingsActivity.class);

            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
            return true;
        });
    }

    /** Delete image from Firebase and update UI */
    private void deleteImage(String url) {
        int eventId = extractEventId(url);
        if (eventId == -1) {
            Toast.makeText(this, "Cannot determine event ID", Toast.LENGTH_SHORT).show();
            return;
        }

        picturesDB.deleteEventImage(eventId, new PicturesDB.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    allImageUrls.remove(url);
                    filteredImageUrls.remove(url);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(AdminImageActivity.this, "Image deleted", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> Toast.makeText(AdminImageActivity.this,
                        "Failed to delete image: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
            }
        });
    }

    /** Extract event ID from URL like ".../event-3.png" */
    private int extractEventId(String url) {
        try {
            String filename = Uri.parse(url).getLastPathSegment(); // "event-3.png"
            if (filename != null) {
                // Look for digits in the filename
                String digits = filename.replaceAll("\\D+", "");
                if (!digits.isEmpty()) return Integer.parseInt(digits);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

}
