package com.example.hotpot0.section1.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.models.UserProfile;
import com.example.hotpot0.section1.controllers.UserSignupValidator;
import com.example.hotpot0.section2.views.HomeActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * UserSignupActivity handles the user registration process.
 * <p>
 * It collects user details (name, email, and phone),
 * validates the inputs, stores the user profile in the database,
 * and redirects the user to the {@code HomeActivity} upon success.
 * </p>
 */
public class UserSignupActivity extends AppCompatActivity {

    private TextInputEditText nameInput, emailInput, phoneInput;
    private MaterialButton createProfileBtn;

    private ProfileDB profileDB;
    private UserSignupValidator signupValidator;

    /**
     * Called when the activity is created.
     * <p>
     * Initializes the views, sets up the database and validator,
     * and attaches a listener to handle profile creation.
     * </p>
     *
     * @param savedInstanceState The saved instance state bundle, if any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section1_signup_activity);

        // ===== Initialize views =====
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        createProfileBtn = findViewById(R.id.buttonCreateProfile);

        // ===== Initialize database and validator =====
        profileDB = new ProfileDB();
        signupValidator = new UserSignupValidator(this, profileDB);

        // ===== Button click =====
        createProfileBtn.setOnClickListener(v -> handleCreateProfile());
    }

    /**
     * Handles user input validation and profile creation.
     * <p>
     * If validation succeeds, saves the user ID in shared preferences
     * and navigates to {@code HomeActivity}. Otherwise, shows an error message.
     * </p>
     */
    private void handleCreateProfile() {
        String name = nameInput.getText() != null ? nameInput.getText().toString() : "";
        String email = emailInput.getText() != null ? emailInput.getText().toString() : "";
        String phone = phoneInput.getText() != null ? phoneInput.getText().toString() : "";

        signupValidator.verifyInputs(name, email, phone, new UserSignupValidator.SignupCallback() {
            @Override
            public void onSuccess(UserProfile user) {
                Toast.makeText(UserSignupActivity.this, "Profile created successfully!", Toast.LENGTH_SHORT).show();
                getSharedPreferences("app_prefs", MODE_PRIVATE)
                        .edit()
                        .putInt("userID", user.getUserID())
                        .apply();
                Intent intent = new Intent(UserSignupActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(UserSignupActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}