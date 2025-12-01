package com.example.hotpot0.section1.views;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.models.UserProfile;

/**
 * MainActivity is the entry point of the app.
 * <p>
 * It checks whether a user profile already exists for the current device.
 * If a user is found, it navigates to {@code HomeActivity};
 * otherwise, it navigates to {@code StartupActivity}.
 * </p>
 */

public class MainActivity extends AppCompatActivity {

    /** Reference to the ProfileDB for accessing user data. */
    private ProfileDB profileDB;

    /**
     * Called when the activity is created.
     * <p>
     * Displays a loading screen, checks the database for an existing user,
     * and navigates to the appropriate activity based on the result.
     * </p>
     *
     * @param savedInstanceState The saved instance state bundle, if any.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Show loading layout while we check the database
        setContentView(R.layout.activity_main);

        // Initialize ProfileDB
        profileDB = new ProfileDB();

        // Get device ID
        String deviceID = getDeviceID();

        // Check if a user with this deviceID exists
        profileDB.getUserByDeviceID(deviceID, new ProfileDB.GetCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile user) {
                // Device exists → go to HomeActivity
                getSharedPreferences("app_prefs", MODE_PRIVATE)
                        .edit()
                        .putInt("userID", user.getUserID())
                        .apply();
                Intent intent = new Intent(MainActivity.this, com.example.hotpot0.section2.views.HomeActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                // Device does not exist → go to StartupActivity
                Intent intent = new Intent(MainActivity.this, com.example.hotpot0.section1.views.StartupActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        });
    }

    /**
     * Returns the unique Android device ID.
     *
     * @return The device ID as a String.
     */
    private String getDeviceID() {
        return android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    }
}
