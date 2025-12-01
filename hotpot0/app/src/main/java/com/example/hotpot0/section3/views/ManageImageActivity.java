package com.example.hotpot0.section3.views;

import android.content.Intent;
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
        String eventName = getIntent().getStringExtra("event_name");
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

        backButton.setOnClickListener(v -> finish());
        removeButton.setOnClickListener(v -> deleteImage());
    }

    private void deleteImage() {
        if (imageUrl == null) return;

        int eventID = extractEventId(imageUrl);

        if (eventID == -1) {
            Toast.makeText(this, "Could not extract event ID from URL", Toast.LENGTH_SHORT).show();
            return;
        }

        picturesDB.deleteEventImage(eventID, new PicturesDB.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(ManageImageActivity.this, "Image deleted", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent();
                intent.putExtra("deleted_image_url", imageUrl);
                setResult(RESULT_OK, intent);

                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ManageImageActivity.this, "Failed to delete image", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private int extractEventId(String url) {
        try {
            String filename = android.net.Uri.parse(url).getLastPathSegment();
            if (filename == null) return -1;

            // Extract only digits from the filename
            String digits = filename.replaceAll("\\D+", ""); // removes everything except digits

            if (digits.isEmpty()) return -1;

            return Integer.parseInt(digits);
        } catch (Exception e) {
            return -1;
        }
    }




}

