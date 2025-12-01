package com.example.hotpot0.section2.views;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;
import java.util.Calendar;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Activity for creating a new event. Users can input event details, upload an image,
 * and preview the event before submission.
 */
public class CreateEventActivity extends AppCompatActivity {

    private ImageView iconUpload;
    private ImageView eventImage;
    private LinearLayout uploadPrompt;
    private Uri eventImageUri;
    private Bitmap eventImageBitmap;
    private MaterialButton deleteImage;
    private MaterialButton previewButton;
    private MaterialCardView uploadSection;
    private SwitchMaterial geolocationStatus;
    private TextInputEditText name, description, guidelines, location, duration, price, capacity, waitingListCapacity;
    private TextInputEditText inputEventTime;
    private TextInputEditText inputEventEndDate;
    private TextInputEditText inputEventStartDate;
    private TextInputEditText inputRegistrationEndDate;
    private TextInputEditText inputRegistrationStartDate;
    private Calendar calendar;
    private Calendar regStartCalendar, regEndCalendar;
    private Calendar eventStartCalendar, eventEndCalendar;
    private SimpleDateFormat dateFormatter;
    private SimpleDateFormat timeFormatter;

    // Activity result launcher for image picker
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            Uri imageUri = result.getData().getData();
                            eventImageUri = imageUri;

                            try {
                                Bitmap originalBitmap;
                                if (Build.VERSION.SDK_INT >= 28) {
                                    ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageUri);
                                    originalBitmap = ImageDecoder.decodeBitmap(source);
                                } else {
                                    originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                                }

                                eventImageBitmap = scaleBitmap(originalBitmap, 800, 800);

                                // Show the image and hide upload prompt
                                eventImage.setImageBitmap(eventImageBitmap);
                                eventImage.setVisibility(View.VISIBLE);
                                uploadPrompt.setVisibility(View.GONE);
                                deleteImage.setVisibility(View.VISIBLE);

                                // Recycle the original bitmap to free memory
                                if (originalBitmap != eventImageBitmap) {
                                    originalBitmap.recycle();
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Image load failed", Toast.LENGTH_SHORT).show();
                            } catch (OutOfMemoryError e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Image too large to load", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );

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

        // Initialize formatters and calendar
        calendar = Calendar.getInstance();
        eventStartCalendar = Calendar.getInstance();
        eventEndCalendar = Calendar.getInstance();
        regStartCalendar = Calendar.getInstance();
        regEndCalendar = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        timeFormatter = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        // Initialize Views
        initializeViews();
        setupBottomNavigation();
        setupClickListeners();
        setupDateAndTimePickers();
        setupFieldWatchers();
        checkRequiredFields();
    }

    /**
     * Initialize all view components
     */
    private void initializeViews() {
        iconUpload = findViewById(R.id.icon_upload);
        eventImage = findViewById(R.id.event_image); // The new ImageView
        uploadPrompt = findViewById(R.id.upload_prompt); // The upload prompt layout
        deleteImage = findViewById(R.id.delete_image);
        uploadSection = findViewById(R.id.upload_section);

        name = findViewById(R.id.input_event_name);
        description = findViewById(R.id.input_event_description);
        guidelines = findViewById(R.id.input_event_guidelines);
        location = findViewById(R.id.input_location);
        duration = findViewById(R.id.input_event_duration);
        price = findViewById(R.id.input_price);
        capacity = findViewById(R.id.input_capacity);
        waitingListCapacity = findViewById(R.id.input_waiting_list_capacity);
        geolocationStatus = findViewById(R.id.switch_geolocation);
        previewButton = findViewById(R.id.button_preview_event);

        // Initialize date and time picker fields
        inputEventTime = findViewById(R.id.input_event_time);
        inputEventStartDate = findViewById(R.id.input_event_start_date);
        inputEventEndDate = findViewById(R.id.input_event_end_date);
        inputRegistrationStartDate = findViewById(R.id.input_registration_start_date);
        inputRegistrationEndDate = findViewById(R.id.input_registration_end_date);
    }

    /**
     * Setup bottom navigation
     */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_events);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                Intent intent = new Intent(CreateEventActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            }

            if (id == R.id.nav_search) {
                Intent intent = new Intent(CreateEventActivity.this, SearchActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            }

            if (id == R.id.nav_notifications) {
                Intent intent = new Intent(CreateEventActivity.this, NotificationsActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            }

            if (id == R.id.nav_profile) {
                Intent intent = new Intent(CreateEventActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            }

            return false;
        });
    }

    /**
     * Setup click listeners for buttons and image upload
     */
    private void setupClickListeners() {
        // Image upload click
        uploadSection.setOnClickListener(v -> openImagePicker());

        // Delete image click
        deleteImage.setOnClickListener(v -> {
            eventImageBitmap = null;
            eventImageUri = null;
            eventImage.setVisibility(View.GONE);
            uploadPrompt.setVisibility(View.VISIBLE);
            deleteImage.setVisibility(View.GONE);
            Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show();
        });

        // Preview button
        previewButton.setOnClickListener(v -> openPreview());
    }

    /**
     * Setup date and time pickers for relevant fields
     */
    private void setupDateAndTimePickers() {
        // Time Picker for Event Time
        inputEventTime.setOnClickListener(v -> showTimePicker(inputEventTime));

        // Event Start Date
//        inputEventStartDate.setOnClickListener(v -> showDatePicker(
//                inputEventStartDate,
//                "Select Event Start Date",
//                (view, year, month, day) -> {
//                    eventStartCalendar.set(year, month, day);
//                    inputEventStartDate.setText(dateFormatter.format(eventStartCalendar.getTime()));
//                }
//        ));
        inputEventStartDate.setOnClickListener(v -> {
            // Ensure registration end date is selected first
            if (TextUtils.isEmpty(inputRegistrationEndDate.getText())) {
                Toast.makeText(this, "Select Registration End Date first", Toast.LENGTH_SHORT).show();
                return;
            }

            DatePickerDialog eventStartPicker = new DatePickerDialog(
                    this,
                    (view, year, month, day) -> {
                        eventStartCalendar.set(year, month, day);
                        inputEventStartDate.setText(dateFormatter.format(eventStartCalendar.getTime()));
                    },
                    eventStartCalendar.get(Calendar.YEAR),
                    eventStartCalendar.get(Calendar.MONTH),
                    eventStartCalendar.get(Calendar.DAY_OF_MONTH)
            );

            // enforce: eventStart ≥ registrationEnd
            long oneDay = 24L * 60 * 60 * 1000;
            eventStartPicker.getDatePicker().setMinDate(regEndCalendar.getTimeInMillis() + oneDay);

            eventStartPicker.setTitle("Select Event Start Date");
            eventStartPicker.show();
        });

        // Event End Date (should be >= start)
        inputEventEndDate.setOnClickListener(v -> {
            inputEventEndDate.setText("");
            DatePickerDialog endPicker = new DatePickerDialog(
                    this,
                    (view, year, month, day) -> {
                        eventEndCalendar.set(year, month, day);
                        inputEventEndDate.setText(dateFormatter.format(eventEndCalendar.getTime()));
                    },
                    eventEndCalendar.get(Calendar.YEAR),
                    eventEndCalendar.get(Calendar.MONTH),
                    eventEndCalendar.get(Calendar.DAY_OF_MONTH)
            );

            endPicker.setTitle("Select Event End Date");
            // Prevent selecting before start date
            if (eventStartCalendar != null) {
                long oneDay = 24L * 60 * 60 * 1000;
                endPicker.getDatePicker().setMinDate(eventStartCalendar.getTimeInMillis() + oneDay);
            }
            endPicker.show();
        });

        // Registration Start Date
        inputRegistrationStartDate.setOnClickListener(v -> showDatePicker(
                inputRegistrationStartDate,
                "Select Registration Start Date",
                (view, year, month, day) -> {
                    regStartCalendar.set(year, month, day);
                    inputRegistrationStartDate.setText(dateFormatter.format(regStartCalendar.getTime()));
                }
        ));

        // Registration End Date (must be >= start)
        inputRegistrationEndDate.setOnClickListener(v -> {
            DatePickerDialog regEndPicker = new DatePickerDialog(
                    this,
                    (view, year, month, day) -> {
                        regEndCalendar.set(year, month, day);
                        inputRegistrationEndDate.setText(dateFormatter.format(regEndCalendar.getTime()));
                    },
                    regEndCalendar.get(Calendar.YEAR),
                    regEndCalendar.get(Calendar.MONTH),
                    regEndCalendar.get(Calendar.DAY_OF_MONTH)
            );

            regEndPicker.setTitle("Select Registration End Date");
            // Prevent selecting before registration start
            if (regStartCalendar != null)
                regEndPicker.getDatePicker().setMinDate(regStartCalendar.getTimeInMillis());
            regEndPicker.show();
        });

    }

    /**
     * Shows a TimePickerDialog and sets the selected time to the EditText
     */
    private void showTimePicker(TextInputEditText editText) {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                    calendar.set(Calendar.MINUTE, selectedMinute);
                    editText.setText(timeFormatter.format(calendar.getTime()));
                },
                hour,
                minute,
                false // 24-hour format set to false for AM/PM format
        );

        timePickerDialog.setTitle("Select Event Time");
        timePickerDialog.show();
    }

    /**
     * Shows a DatePickerDialog and sets the selected date to the EditText
     */
    private void showDatePicker(TextInputEditText editText, String title, DatePickerDialog.OnDateSetListener onDateSetListener) {
        Calendar currentDate = Calendar.getInstance();
        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH);
        int day = currentDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                onDateSetListener,
                year,
                month,
                day
        );

        datePickerDialog.setTitle(title);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void openImagePicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13+, no permission needed for image picker
            launchImagePicker();
        } else {
            // For older versions, request READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                launchImagePicker();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1001);
            }
        }
    }

    private void launchImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchImagePicker();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Opens the preview activity, passing all entered event details as extras.
     * Validates that required fields are filled.
     */
    private void openPreview() {
        // Validate required fields
        if (!validateRequiredFields()) {
            return;
        }

        Intent intent = new Intent(this, PreviewActivity.class);

        // Pass all data to preview activity
        intent.putExtra("imageUri", eventImageUri != null ? eventImageUri.toString() : null);
        intent.putExtra("name", name.getText().toString().trim());
        intent.putExtra("description", description.getText().toString().trim());
        intent.putExtra("guidelines", guidelines.getText().toString().trim());
        intent.putExtra("location", location.getText().toString().trim());
        intent.putExtra("time", inputEventTime.getText().toString().trim());
        intent.putExtra("startDate", inputEventStartDate.getText().toString().trim());
        intent.putExtra("endDate", inputEventEndDate.getText().toString().trim());
        intent.putExtra("duration", duration.getText().toString().trim());
        intent.putExtra("price", price.getText().toString().trim());
        intent.putExtra("capacity", capacity.getText().toString().trim());
        intent.putExtra("waitingListCapacity", waitingListCapacity.getText().toString().trim());
        intent.putExtra("registrationStart", inputRegistrationStartDate.getText().toString().trim());
        intent.putExtra("registrationEnd", inputRegistrationEndDate.getText().toString().trim());
        intent.putExtra("geolocationEnabled", geolocationStatus.isChecked());

        // Pass URI instead of bitmap
        if (eventImageUri != null) {
            intent.putExtra("imageUri", eventImageUri.toString());
        } else if (eventImageBitmap != null) {
            // You might want to compress the bitmap or pass as URI instead
            // For now, we'll pass it as extra (be careful with size limits)
            intent.putExtra("imageBitmap", eventImageBitmap);
        }

        startActivity(intent);
    }

    /**
     * Validates that all required fields are filled
     */
    private boolean validateRequiredFields() {

        // Convert to timestamps for comparison
        long today = toDateOnlyMillis(Calendar.getInstance());
        long regStart = toDateOnlyMillis(regStartCalendar);
        long regEnd = toDateOnlyMillis(regEndCalendar);
        long eventStart = toDateOnlyMillis(eventStartCalendar);
        long eventEnd = toDateOnlyMillis(eventEndCalendar);

        // Registration Start Date ≥ Today ---
        if (regStart < today) {
            inputRegistrationStartDate.setError("Registration start cannot be in the past");
            inputRegistrationStartDate.requestFocus();
            return false;
        }

        // Registration End Date ≥ Registration Start Date ---
        if (regEnd < regStart) {
            inputRegistrationEndDate.setError("Registration end must be after registration start");
            inputRegistrationEndDate.requestFocus();
            return false;
        }

        // Event Start Date ≥ Registration End Date + 1 day ---
        long minEventStart = regEnd + 24 * 60 * 60 * 1000; // +1 day
        if (eventStart < minEventStart) {
            inputEventStartDate.setError("Event must start at least 1 day after registration ends");
            inputEventStartDate.requestFocus();
            return false;
        }

        // Event End Date ≥ Event Start Date (if end date provided) ---
        if (!TextUtils.isEmpty(inputEventEndDate.getText().toString().trim())) {
            if (eventEnd < eventStart) {
                inputEventEndDate.setError("Event end must be after event start");
                inputEventEndDate.requestFocus();
                return false;
            }
        }

        if (TextUtils.isEmpty(name.getText().toString().trim())) {
            name.setError("Required");
            name.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(description.getText().toString().trim())) {
            description.setError("Required");
            description.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(guidelines.getText().toString().trim())) {
            guidelines.setError("Required");
            guidelines.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(location.getText().toString().trim())) {
            location.setError("Required");
            location.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(duration.getText().toString().trim())) {
            duration.setError("Required");
            duration.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(price.getText().toString().trim())) {
            price.setError("Required");
            price.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(capacity.getText().toString().trim())) {
            capacity.setError("Required");
            capacity.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(inputEventTime.getText().toString().trim())) {
            inputEventTime.setError("Required");
            inputEventTime.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(inputEventStartDate.getText().toString().trim())) {
            inputEventStartDate.setError("Required");
            inputEventStartDate.requestFocus();
            return false;
        }

        // Maybe we should keep Event End Date as optional

//        if (TextUtils.isEmpty(inputEventEndDate.getText().toString().trim())) {
//            inputEventEndDate.setError("Required");
//            inputEventEndDate.requestFocus();
//            return false;
//        }

        if (TextUtils.isEmpty(inputRegistrationStartDate.getText().toString().trim())) {
            inputRegistrationStartDate.setError("Required");
            inputRegistrationStartDate.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(inputRegistrationEndDate.getText().toString().trim())) {
            inputRegistrationEndDate.setError("Required");
            inputRegistrationEndDate.requestFocus();
            return false;
        }

        // Price validation
        String priceStr = price.getText().toString().trim();
        if (TextUtils.isEmpty(priceStr)) {
            price.setError("Required");
            price.requestFocus();
            return false;
        }
        try {
            double priceValue = Double.parseDouble(priceStr);
            if (priceValue < 0) {
                price.setError("Must be positive");
                price.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            price.setError("Must be a number");
            price.requestFocus();
            return false;
        }

        // Capacity validation
        String capacityStr = capacity.getText().toString().trim();
        if (TextUtils.isEmpty(capacityStr)) {
            capacity.setError("Required");
            capacity.requestFocus();
            return false;
        }
        int capacityValue;
        try {
            capacityValue = Integer.parseInt(capacityStr);
            if (capacityValue <= 0) {
                capacity.setError("Must be positive");
                capacity.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            capacity.setError("Must be a number");
            capacity.requestFocus();
            return false;
        }

        // Waiting list capacity validation (optional, same as above)
        String waitingListStr = waitingListCapacity.getText().toString().trim();
        capacityValue = Integer.parseInt(capacityStr);
        if (!TextUtils.isEmpty(waitingListStr)) {
            try {
                int waitingListValue = Integer.parseInt(waitingListStr);
                if (waitingListValue < 0) {
                    waitingListCapacity.setError("Must be zero or positive");
                    waitingListCapacity.requestFocus();
                    return false;
                }
                if (waitingListValue < capacityValue) {
                    waitingListCapacity.setError("Waiting list must be ≥ capacity");
                    waitingListCapacity.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                waitingListCapacity.setError("Must be a number");
                waitingListCapacity.requestFocus();
                return false;
            }
        }
        return true;
    }
    private long toDateOnlyMillis(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    /**
     * Sets up text watchers on required fields to enable/disable the preview button.
     */
    private void setupFieldWatchers() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                checkRequiredFields();
            }
        };

        // Add watcher to all required fields
        name.addTextChangedListener(watcher);
        description.addTextChangedListener(watcher);
        guidelines.addTextChangedListener(watcher);
        location.addTextChangedListener(watcher);
        duration.addTextChangedListener(watcher);
        price.addTextChangedListener(watcher);
        capacity.addTextChangedListener(watcher);
        inputEventTime.addTextChangedListener(watcher);
        inputEventStartDate.addTextChangedListener(watcher);
        inputRegistrationStartDate.addTextChangedListener(watcher);
        inputRegistrationEndDate.addTextChangedListener(watcher);
    }

    /**
     * Checks if all required fields are filled and enables/disables the preview button accordingly.
     */
    private void checkRequiredFields() {
        boolean allFilled = !TextUtils.isEmpty(name.getText().toString().trim())
                && !TextUtils.isEmpty(description.getText().toString().trim())
                && !TextUtils.isEmpty(guidelines.getText().toString().trim())
                && !TextUtils.isEmpty(location.getText().toString().trim())
                && !TextUtils.isEmpty(duration.getText().toString().trim())
                && !TextUtils.isEmpty(price.getText().toString().trim())
                && !TextUtils.isEmpty(capacity.getText().toString().trim())
                && !TextUtils.isEmpty(inputEventTime.getText().toString().trim())
                && !TextUtils.isEmpty(inputEventStartDate.getText().toString().trim())
                && !TextUtils.isEmpty(inputRegistrationStartDate.getText().toString().trim())
                && !TextUtils.isEmpty(inputRegistrationEndDate.getText().toString().trim());

        previewButton.setEnabled(allFilled);
    }

    /**
     * Scales a bitmap to fit within the specified max width and height while maintaining aspect ratio.
     *
     * @param original The original bitmap to scale.
     * @param maxWidth The maximum width.
     * @param maxHeight The maximum height.
     * @return The scaled bitmap.
     */
    private Bitmap scaleBitmap(Bitmap original, int maxWidth, int maxHeight) {
        try {
            int originalWidth = original.getWidth();
            int originalHeight = original.getHeight();

            // Calculate aspect ratio
            float aspectRatio = (float) originalWidth / originalHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;

            if (originalWidth > originalHeight) {
                // Landscape
                finalHeight = (int) (maxWidth / aspectRatio);
            } else {
                // Portrait or square
                finalWidth = (int) (maxHeight * aspectRatio);
            }

            // Ensure dimensions don't exceed max values
            if (finalWidth > maxWidth) {
                finalWidth = maxWidth;
                finalHeight = (int) (finalWidth / aspectRatio);
            }
            if (finalHeight > maxHeight) {
                finalHeight = maxHeight;
                finalWidth = (int) (finalHeight * aspectRatio);
            }

            return Bitmap.createScaledBitmap(original, finalWidth, finalHeight, true);
        } catch (Exception e) {
            e.printStackTrace();
            return original;
        }
    }
}