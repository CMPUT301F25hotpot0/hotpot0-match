package com.example.hotpot0.section2.views;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;

import java.io.IOException;

/**
 * Activity for creating a new event. Users can input event details, upload an image,
 * and preview the event before submission.
 */
public class CreateEventActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView iconUpload, deleteImage;
    private LinearLayout uploadSection;
    private Bitmap eventImageBitmap;

    private EditText name, description, guidelines, location, time, date, duration, price, capacity, registrationPeriod;
    private Switch GeolocationStatus;
    private Button backButtonCreateEvents;
    private Button previewButton;

    /**
     * Called when the activity is first created. Initializes all views, sets click listeners
     * for uploading/deleting images, previewing the event, and navigating back.
     *
     * @param savedInstanceState The saved instance state bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.section2_createevent_activity);

        // Initialize Views
        iconUpload = findViewById(R.id.icon_upload);
        deleteImage = findViewById(R.id.delete_image);
        uploadSection = findViewById(R.id.upload_section);

        name = findViewById(R.id.input_event_name);
        description = findViewById(R.id.input_event_description);
        guidelines = findViewById(R.id.input_event_guidelines);
        location = findViewById(R.id.input_location);
        time = findViewById(R.id.input_event_time);
        date = findViewById(R.id.input_event_date);
        duration = findViewById(R.id.input_event_duration);
        price = findViewById(R.id.input_price);
        capacity = findViewById(R.id.input_capacity);
        registrationPeriod = findViewById(R.id.input_registration_period);
        GeolocationStatus = findViewById(R.id.switch_geolocation);
        backButtonCreateEvents = findViewById(R.id.button_BackCreateEvent);
        previewButton = findViewById(R.id.button_preview_event);

        // Image upload click
        uploadSection.setOnClickListener(v -> openImagePicker());

        // Delete image click
        deleteImage.setOnClickListener(v -> {
            eventImageBitmap = null;
            iconUpload.setImageResource(R.drawable.ic_camera);
            deleteImage.setVisibility(View.GONE);
            Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show();
        });

        // Preview button
        previewButton.setOnClickListener(v -> openPreview());

        backButtonCreateEvents.setOnClickListener(v -> {
            Intent intent = new Intent(CreateEventActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });
    }

    /**
     * Opens the image picker so the user can select an image from their gallery.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /**
     * Handles the result of the image picker activity.
     * Loads the selected image into the ImageView and stores it as a Bitmap.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult().
     * @param resultCode  The integer result code returned by the child activity.
     * @param data        The intent returned by the child activity, containing image URI.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                eventImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                iconUpload.setImageBitmap(eventImageBitmap);
                deleteImage.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Image load failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Opens the preview activity, passing all entered event details as extras.
     * Validates that required fields (event name and description) are filled.
     */
    private void openPreview() {
        // Collect data
        String eventName = name.getText().toString().trim();
        String desc = description.getText().toString().trim();

        if (eventName.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Please fill out event name and description", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, PreviewActivity.class);
        intent.putExtra("name", eventName);
        intent.putExtra("description", desc);
        intent.putExtra("guidelines", guidelines.getText().toString().trim());
        intent.putExtra("location", location.getText().toString().trim());
        intent.putExtra("time", time.getText().toString().trim());
        intent.putExtra("date", date.getText().toString().trim());
        intent.putExtra("duration", duration.getText().toString().trim());
        intent.putExtra("price", price.getText().toString().trim());
        intent.putExtra("capacity", capacity.getText().toString().trim());
        intent.putExtra("registration", registrationPeriod.getText().toString().trim());
        intent.putExtra("geolocationEnabled", GeolocationStatus.isChecked());

        startActivity(intent);
    }
}

