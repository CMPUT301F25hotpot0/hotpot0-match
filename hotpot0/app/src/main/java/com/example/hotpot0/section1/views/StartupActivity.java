package com.example.hotpot0.section1.views;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.hotpot0.R;
import com.example.hotpot0.models.AdminProfile;
import com.example.hotpot0.section3.views.AdminHomeActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * StartupActivity is the first screen shown when no existing user profile is found.
 * <p>
 * It allows the user to either create a new profile or log in as an admin.
 * </p>
 */
public class StartupActivity extends AppCompatActivity {

    /** Button for navigating to the user sign-up screen. */
    private MaterialButton createProfileButton;
    /** Button for navigating to the admin login screen. */
    private MaterialButton adminLoginButton;


    /**
     * Called when the activity is created.
     * <p>
     * Initializes the UI, sets up button listeners, and handles navigation
     * to the appropriate screens based on user interaction.
     * </p>
     *
     * @param savedInstanceState The saved instance state bundle, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section1_startup_activity); // <-- ensure this is the XML filename

        // Find views (IDs must match the XML layout)
        createProfileButton = findViewById(R.id.createProfileButton);
        adminLoginButton = findViewById(R.id.adminLoginButton);

        // Navigate to UserSignupActivity when the user clicks "Create Profile"
        createProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start UserSignUpActivity
                Intent intent = new Intent(StartupActivity.this, UserSignupActivity.class);
                startActivity(intent);
                // finish() if StartupActivity on the back stack not required
            }
        });

        // Navigate to AdminLoginActivity when the user clicks "Admin Login"
//        adminLoginButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Start AdminLoginActivity
//                // Intent intent = new Intent(StartupActivity.this, AdminLoginActivity.class);
//                // startActivity(intent);
//            }
//        });

        // Implementing Admin login with only deviceID
        adminLoginButton.setOnClickListener(v -> {
            String deviceID = Settings.Secure.getString(
                    getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );
            Log.d("DEVICE_ID", "Device ID = " + deviceID);

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("AdminProfiles")
                    .whereEqualTo("deviceID", deviceID)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            // Admin found
                            AdminProfile admin = querySnapshot.getDocuments()
                                    .get(0)
                                    .toObject(AdminProfile.class);

                            if (admin != null) {
                                Intent intent = new Intent(
                                        StartupActivity.this,
                                        AdminHomeActivity.class
                                );
                                intent.putExtra("adminID", admin.getAdminID());

                                startActivity(intent);
                                finish();
                            }
                        } else {
                            // Not an admin
                            Toast.makeText(
                                    StartupActivity.this,
                                    "User does not have admin privileges",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(
                                StartupActivity.this,
                                "Error checking admin status: " + e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    });
        });
    }
}