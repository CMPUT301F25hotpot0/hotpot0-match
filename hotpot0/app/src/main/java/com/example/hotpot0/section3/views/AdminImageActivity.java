package com.example.hotpot0.section3.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.EventDB;
import com.example.hotpot0.models.PicturesDB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.hotpot0.R;

public class AdminImageActivity extends AppCompatActivity {
    private PicturesDB picturesDB;
    RecyclerView recyclerView;
    private EditText searchEditText;
    private ArrayList<String> allImageUrls = new ArrayList<>();
    private ArrayList<String> filteredImageUrls = new ArrayList<>();
    private AdminImageAdapter adapter;
    private EventDB eventDB = new EventDB();
    private Map<Integer, String> eventNames = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section3_adminsearchimage_activity);

        picturesDB = new PicturesDB();

        searchEditText = findViewById(R.id.search_edit_text);
        recyclerView = findViewById(R.id.imagesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminImageAdapter(
                this,
                filteredImageUrls,
                url -> deleteImage(url),
                url -> {
                    Intent intent = new Intent(this, ManageImageActivity.class);
                    intent.putExtra("image_url", url);
                    startActivity(intent);
                },
                eventNames,
                url -> extractEventId(url)
        );

        recyclerView.setAdapter(adapter);

        loadImages();
        setupSearch();
    }

    private void loadImages() {
        picturesDB.getAllEventImages(new PicturesDB.Callback<List<String>>() {
            @Override
            public void onSuccess(List<String> urls) {
                allImageUrls.clear();
                allImageUrls.addAll(urls);

                filteredImageUrls.clear();
                filteredImageUrls.addAll(urls);

                // Preload event names
                for (String url : urls) {
                    int eventId = extractEventId(url);
                    eventDB.getEventByID(eventId, new EventDB.GetCallback<Event>() {
                        @Override
                        public void onSuccess(Event event) {
                            if (event != null) {
                                eventNames.put(eventId, event.getName());
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            eventNames.put(eventId, "Unknown Event");
                            adapter.notifyDataSetChanged();
                        }
                    });
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminImageActivity.this, "Failed to load images", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterImages(s.toString());
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void filterImages(String query) {
        filteredImageUrls.clear();

        for (String url : allImageUrls) {
            String filename = Uri.parse(url).getLastPathSegment();
            if (filename != null && filename.toLowerCase().contains(query.toLowerCase())) {
                filteredImageUrls.add(url);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void deleteImage(String url) {
        int eventID = extractEventId(url);

        picturesDB.deleteEventImage(eventID, new PicturesDB.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                allImageUrls.remove(url);
                filteredImageUrls.remove(url);
                adapter.notifyDataSetChanged();

                Toast.makeText(AdminImageActivity.this, "Deleted image", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AdminImageActivity.this, "Delete failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int extractEventId(String url) {
        // Remove folder prefix
        String name = Uri.parse(url).getPath(); // e.g., "/event_images/3"
        if (name == null) return -1;

        // Split by '/' and take the last part
        String[] parts = name.split("/");
        String lastPart = parts[parts.length - 1];

        try {
            return Integer.parseInt(lastPart); // should be 3
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return -1;
        }
    }

}
