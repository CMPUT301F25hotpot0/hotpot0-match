package com.example.hotpot0.section3.views;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import com.example.hotpot0.R;

public class AdminImageActivity extends AppCompatActivity {
    private EditText searchEditText;
    private GridView imagesGridView;
    private ArrayList<String> imageNames = new ArrayList<>();
    private ArrayList<String> imageUrls = new ArrayList<>();
    private ArrayList<String> filteredNames = new ArrayList<>();
    private ArrayList<String> filteredUrls = new ArrayList<>();
    private AdminImageAdapter adapter;
    private StorageReference storageRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section3_adminsearchimage_activity);

        searchEditText = findViewById(R.id.searchEditText);
        imagesGridView = findViewById(R.id.imagesGridView);

        storageRef = FirebaseStorage.getInstance().getReference("EventImages");

        loadImages();

        // Search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterImages(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }


    private void loadImages() {
        storageRef.listAll().addOnSuccessListener(listResult -> {
            imageNames.clear();
            imageUrls.clear();

            if (listResult.getItems().isEmpty()) {
                Toast.makeText(this, "No images found", Toast.LENGTH_SHORT).show();
                return;
            }

            for (StorageReference fileRef : listResult.getItems()) {

                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {

                    imageNames.add(fileRef.getName());
                    imageUrls.add(uri.toString());

                    if (imageUrls.size() == listResult.getItems().size()) {

                        // Initially show all images
                        filteredNames = new ArrayList<>(imageNames);
                        filteredUrls = new ArrayList<>(imageUrls);

                        adapter = new AdminImageAdapter(AdminImageActivity.this, filteredUrls);
                        imagesGridView.setAdapter(adapter);
                    }

                });
            }

        })
        .addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load images", Toast.LENGTH_SHORT).show();
        });
}

    private void filterImages(String query) {
        filteredNames.clear();
        filteredUrls.clear();

        for (int i = 0; i < imageNames.size(); i++) {
            if (imageNames.get(i).toLowerCase().contains(query.toLowerCase())) {
                filteredNames.add(imageNames.get(i));
                filteredUrls.add(imageUrls.get(i));
            }
        }
        adapter.updateData(filteredUrls);
    }
}
