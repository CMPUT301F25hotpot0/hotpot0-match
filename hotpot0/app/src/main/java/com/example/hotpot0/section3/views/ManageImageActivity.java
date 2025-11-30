package com.example.hotpot0.section3.views;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hotpot0.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class ManageImageActivity extends AppCompatActivity {
    private String imageId, imageUrl, eventName;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section3_managepicture_activity);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Retrieve data sent from AdminImageActivity
        imageId = getIntent().getStringExtra("imageId");
        imageUrl = getIntent().getStringExtra("imageUrl");
        eventName = getIntent().getStringExtra("eventName");

        ImageButton backButton = findViewById(R.id.backButton);
        MaterialButton removeButton = findViewById(R.id.removeImageButton);
        TextView title = findViewById(R.id.headerLayout).findViewById(R.id.title);

        backButton.setOnClickListener(v -> finish());

        // Set title dynamically
        TextView titleText = findViewById(R.id.headerLayout).findViewById(R.id.textView);
        titleText.setText(eventName);

        removeButton.setOnClickListener(v -> deleteImage());
    }

    private void deleteImage() {

        storage.getReferenceFromUrl(imageUrl).delete().addOnSuccessListener(a -> {
            db.collection("Images").document(imageId).delete().addOnSuccessListener(v -> {
                Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show();
                finish();
            });
        })
        .addOnFailureListener(e ->
                Toast.makeText(this, "Failed to delete image file", Toast.LENGTH_SHORT).show());
    }
}

