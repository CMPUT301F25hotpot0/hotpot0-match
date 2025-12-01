package com.example.hotpot0.section3.views;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.hotpot0.R;
import com.example.hotpot0.models.PicturesDB;
import com.google.android.material.button.MaterialButton;

public class ManageImageActivity extends AppCompatActivity {
    private ImageView selectedImageView;
    private TextView filenameTextView;
    private MaterialButton removeButton;
    private ImageButton backButton;
    private PicturesDB picturesDB;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section3_managepicture_activity);

        picturesDB = new PicturesDB();

        selectedImageView = findViewById(R.id.selected_image_view);
        filenameTextView = findViewById(R.id.image_filename);
        removeButton = findViewById(R.id.remove_image_button);
        backButton = findViewById(R.id.backButton);

        // Get image URL passed from AdminImageActivity
        imageUrl = getIntent().getStringExtra("image_url");
        String eventName = getIntent().getStringExtra("name");
        if (imageUrl != null) {
            Glide.with(this).load(imageUrl).into(selectedImageView);

            if (eventName != null) {
                filenameTextView.setText(eventName);
            } else {
                // Fallback: show filename if event name not passed
                String filename = android.net.Uri.parse(imageUrl).getLastPathSegment();
                filenameTextView.setText(filename);
            }
        }

        // Back button
        backButton.setOnClickListener(v -> finish());

        // Remove image button
        removeButton.setOnClickListener(v -> deleteImage());
    }

    private void deleteImage() {
        if (imageUrl == null) return;

        int eventID = Integer.parseInt(android.net.Uri.parse(imageUrl).getLastPathSegment().replace("event-", "").replace(".png", ""));

        picturesDB.deleteEventImage(eventID, new PicturesDB.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(ManageImageActivity.this, "Image deleted", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ManageImageActivity.this, "Failed to delete image", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

