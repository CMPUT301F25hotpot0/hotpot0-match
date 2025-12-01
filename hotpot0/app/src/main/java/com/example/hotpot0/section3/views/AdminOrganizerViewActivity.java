package com.example.hotpot0.section3.views;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hotpot0.R;
import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.EventDB;
import com.example.hotpot0.models.EventUserLinkDB;
import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.models.UserProfile;

import java.util.ArrayList;
import java.util.List;

public class AdminOrganizerViewActivity extends AppCompatActivity {

    private ProfileDB profileDB;
    private EventDB eventDB;
    private EventUserLinkDB linkDB;

    private int organizerID;

    private TextView organizerName, organizerEmail, organizerPhone, organizerUserID;
    private Button removeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section3_adminorganizer_view_activity);

        profileDB = new ProfileDB();
        eventDB = new EventDB();
        linkDB = new EventUserLinkDB();

        organizerID = getIntent().getIntExtra("organizerID", -1);

        organizerName = findViewById(R.id.organizerName);
        organizerEmail = findViewById(R.id.organizerEmail);
        organizerPhone = findViewById(R.id.organizerPhone);
        organizerUserID = findViewById(R.id.organizerUserID);
        removeButton = findViewById(R.id.removeOrganizerButton);

        loadOrganizer();

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        removeButton.setOnClickListener(v -> showConfirmDeleteDialog());
    }

    private void loadOrganizer() {
        if (organizerID < 0) {
            Toast.makeText(this, "Invalid organizer ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        profileDB.getUserByID(organizerID, new ProfileDB.GetCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile user) {
                organizerName.setText(user.getName());
                organizerEmail.setText(user.getEmailID());
                organizerPhone.setText(user.getPhoneNumber());
                organizerUserID.setText(String.valueOf(user.getUserID()));
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(
                        AdminOrganizerViewActivity.this,
                        "Failed to load organizer",
                        Toast.LENGTH_SHORT
                ).show();
                finish();
            }
        });
    }

    // ------------------------ CONFIRMATION DIALOG (OPTION 2) ------------------------
    private void showConfirmDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Remove Organizer?")
                .setMessage("Deleting this organizer will also remove ALL their events and registrations. This cannot be undone.\n\nAre you sure?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    removeButton.setEnabled(false);
                    cascadeDeleteOrganizer();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ------------------------ CASCADE DELETE ------------------------
    private void cascadeDeleteOrganizer() {

        // 1) Fetch all events
        eventDB.getAllEvents(new EventDB.GetCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> events) {

                // Filter to events belonging to this organizer
                List<Event> toDelete = new ArrayList<>();
                for (Event e : events) {
                    if (e.getOrganizerID() != null && e.getOrganizerID() == organizerID) {
                        toDelete.add(e);
                    }
                }

                if (toDelete.isEmpty()) {
                    // No events -> just delete organizer profile
                    deleteOrganizerProfile();
                    return;
                }

                // We have events to delete → delete them + their links, then the organizer
                final int[] remaining = {toDelete.size()};

                for (Event e : toDelete) {
                    deleteEventWithLinks(e, () -> {
                        remaining[0]--;
                        if (remaining[0] == 0) {
                            // All events processed → now delete organizer profile
                            deleteOrganizerProfile();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(
                        AdminOrganizerViewActivity.this,
                        "Failed to load events for deletion: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
                removeButton.setEnabled(true);
            }
        });
    }

    /**
     * Deletes all EventUserLinks for this event (based on its linkIDs),
     * then deletes the Event document itself.
     */
    private void deleteEventWithLinks(Event event, Runnable onDone) {
        List<String> linkIDs = event.getLinkIDs();

        // Delete all EventUserLink docs (best-effort, we don't block on each one)
        if (linkIDs != null) {
            for (String linkID : linkIDs) {
                linkDB.deleteEventUserLink(linkID, new EventUserLinkDB.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        // no-op
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // also no-op: we still continue overall deletion
                    }
                });
            }
        }

        // Now delete the event itself
        eventDB.deleteEvent(event.getEventID(), new ProfileDB.ActionCallback() {
            @Override
            public void onSuccess() {
                onDone.run();
            }

            @Override
            public void onFailure(Exception e) {
                // even if one event fails, we still proceed with the rest
                onDone.run();
            }
        });
    }

    /**
     * Deletes the organizer's UserProfile.
     */
    private void deleteOrganizerProfile() {
        profileDB.deleteUser(organizerID, new ProfileDB.ActionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(
                        AdminOrganizerViewActivity.this,
                        "Organizer and all their events removed.",
                        Toast.LENGTH_LONG
                ).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(
                        AdminOrganizerViewActivity.this,
                        "Failed to delete organizer profile: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
                removeButton.setEnabled(true);
            }
        });
    }
}
