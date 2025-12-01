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

    private TextView organizerName, organizerUserID;
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

    private void showConfirmDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Remove Organizer?")
                .setMessage("Deleting this organizer will also remove ALL their events and registrations.\n\nThis cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    removeButton.setEnabled(false);
                    cascadeDeleteOrganizer();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void cascadeDeleteOrganizer() {
        eventDB.getAllEvents(new EventDB.GetCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> events) {

                List<Event> toDelete = new ArrayList<>();
                for (Event e : events) {
                    if (e.getOrganizerID() != null && e.getOrganizerID() == organizerID) {
                        toDelete.add(e);
                    }
                }

                if (toDelete.isEmpty()) {
                    deleteOrganizerProfile();
                    return;
                }

                final int[] remaining = {toDelete.size()};

                for (Event e : toDelete) {
                    deleteEventWithLinks(e, () -> {
                        remaining[0]--;
                        if (remaining[0] == 0) {
                            deleteOrganizerProfile();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(
                        AdminOrganizerViewActivity.this,
                        "Failed to load events for deletion.",
                        Toast.LENGTH_LONG
                ).show();
                removeButton.setEnabled(true);
            }
        });
    }

    // 🔥 NEW METHOD — removes a linkID from ALL users' linkIDs arrays
    private void removeEventLinkFromAllUsers(String linkID) {

        profileDB.getAllUsers(new ProfileDB.GetCallback<List<UserProfile>>() {
            @Override
            public void onSuccess(List<UserProfile> users) {

                for (UserProfile user : users) {
                    List<String> links = user.getLinkIDs();

                    if (links != null && links.contains(linkID)) {

                        profileDB.removeLinkIDFromUser(
                                user,
                                linkID,
                                new ProfileDB.GetCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                        // removed successfully
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        // ignore, continue
                                    }
                                }
                        );
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                // ignore
            }
        });
    }

    private void deleteEventWithLinks(Event event, Runnable onDone) {
        List<String> linkIDs = event.getLinkIDs();

        if (linkIDs != null) {
            for (String linkID : linkIDs) {

                removeEventLinkFromAllUsers(linkID);

                // delete EventUserLink doc
                linkDB.deleteEventUserLink(linkID, new EventUserLinkDB.ActionCallback() {
                    @Override public void onSuccess() {}
                    @Override public void onFailure(Exception e) {}
                });
            }
        }

        // delete event document
        eventDB.deleteEvent(event.getEventID(), new ProfileDB.ActionCallback() {
            @Override
            public void onSuccess() { onDone.run(); }

            @Override
            public void onFailure(Exception e) { onDone.run(); }
        });
    }

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
                        "Failed to delete organizer profile.",
                        Toast.LENGTH_LONG
                ).show();
                removeButton.setEnabled(true);
            }
        });
    }
}
