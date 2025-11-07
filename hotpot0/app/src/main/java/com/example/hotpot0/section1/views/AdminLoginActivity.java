package com.example.hotpot0.section1.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.models.AdminProfile;
import com.example.hotpot0.section1.controllers.AdminLoginVerifier;
import com.example.hotpot0.section3.views.AdminHomeActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * AdminLoginActivity handles the login process for administrators.
 * <p>
 * It collects admin credentials, validates them through
 * {@link AdminLoginVerifier}, and navigates to the {@code AdminHomeActivity}
 * upon successful authentication.
 * </p>
 */
public class AdminLoginActivity extends AppCompatActivity {

    private TextInputEditText usernameInput;
    private TextInputEditText passwordInput;
    private MaterialButton loginButton;

    private ProfileDB profileDB;
    private AdminLoginVerifier loginVerifier;

    /**
     * Called when the activity is created.
     * <p>
     * Initializes the user interface, sets up database and verifier instances,
     * and attaches a click listener to handle login attempts.
     * </p>
     *
     * @param savedInstanceState The saved instance state bundle, if any.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section1_adminlogin_activity);

        // Initialize UI elements
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.buttonLogin);

        // Initialize ProfileDB and LoginVerifier
        profileDB = new ProfileDB();
        loginVerifier = new AdminLoginVerifier(profileDB);

        // Set button click listener
        loginButton.setOnClickListener(v -> attemptLogin());
    }

    /**
     * Attempts to log in the admin by verifying their credentials.
     * <p>
     * If successful, navigates to {@code AdminHomeActivity};
     * otherwise, shows an error message.
     * </p>
     */
    private void attemptLogin() {
        String username = usernameInput.getText() != null ? usernameInput.getText().toString().trim() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString() : "";

        loginVerifier.verifyCredentials(username, password, new AdminLoginVerifier.AuthCallback() {
            @Override
            public void onSuccess(AdminProfile admin) {
                // Successful login
                Toast.makeText(AdminLoginActivity.this, "Admin logged in", Toast.LENGTH_SHORT).show();
                // You can proceed to next activity here if needed
                Intent intent = new Intent(AdminLoginActivity.this, AdminHomeActivity.class);
                intent.putExtra("adminID", admin.getAdminID());
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                // Failed login
                Toast.makeText(AdminLoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}