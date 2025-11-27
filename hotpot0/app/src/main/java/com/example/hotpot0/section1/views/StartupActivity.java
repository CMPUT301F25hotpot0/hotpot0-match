package com.example.hotpot0.section1.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.hotpot0.R;
import com.google.android.material.button.MaterialButton;

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
        adminLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start AdminLoginActivity
                Intent intent = new Intent(StartupActivity.this, AdminLoginActivity.class);
                startActivity(intent);
            }
        });
    }
}