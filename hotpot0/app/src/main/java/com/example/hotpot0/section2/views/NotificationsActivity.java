package com.example.hotpot0.section2.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotpot0.R;
import com.example.hotpot0.models.EventUserLink;
import com.example.hotpot0.models.EventUserLinkDB;
import com.example.hotpot0.models.Notification;
import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.models.UserProfile;
import com.example.hotpot0.section2.controllers.EventActivityController;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView notificationsRecycler;
    private TextView emptyInvitationsText;
    private NotificationAdapter adapter;
    private List<Notification> notifications = new ArrayList<>();

    private EventUserLinkDB eventUserLinkDB;
    private ProfileDB profileDB;
    private Integer userID;
    private EventActivityController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section2_notifications_activity);

        notificationsRecycler = findViewById(R.id.notifications_recycler);
        emptyInvitationsText = findViewById(R.id.emptyInvitationsText);

        notificationsRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(notifications);
        notificationsRecycler.setAdapter(adapter);

        eventUserLinkDB = new EventUserLinkDB();
        profileDB = new ProfileDB();
        userID = getSharedPreferences("app_prefs", MODE_PRIVATE).getInt("userID", -1);

        if (userID == null) {
            emptyInvitationsText.setText("User not found.");
            emptyInvitationsText.setVisibility(View.VISIBLE);
            return;
        }

        fetchNotifications();
        setupBottomNavigation();
    }

    private void fetchNotifications() {
        profileDB.getUserByID(userID, new ProfileDB.GetCallback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile userProfile) {
                if (userProfile.getLinkIDs() == null || userProfile.getLinkIDs().isEmpty()) {
                    emptyInvitationsText.setVisibility(View.VISIBLE);
                    return;
                }

                List<String> linkIDs = userProfile.getLinkIDs();
                final int totalLinks = linkIDs.size();
                final int[] completedLinks = {0};

                for (String linkID : linkIDs) {
                    eventUserLinkDB.getEventUserLinkByID(linkID, new EventUserLinkDB.GetCallback<EventUserLink>() {
                        @Override
                        public void onSuccess(EventUserLink eventUserLink) {
                            List<Notification> notifs = eventUserLink.getNotifications();
                            if (notifs != null) {
                                notifications.addAll(notifs);
                            }

                            completedLinks[0]++;

                            if (completedLinks[0] == totalLinks) {
                                if (notifications.isEmpty()) {
                                    emptyInvitationsText.setVisibility(View.VISIBLE);
                                } else {
                                    emptyInvitationsText.setVisibility(View.GONE);
                                    sortNotificationsNewestFirst();
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            completedLinks[0]++;
                            if (completedLinks[0] == totalLinks) {
                                sortNotificationsNewestFirst();
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Handle failure
            }
        });
    }

    // -------------------------
    // SORT USING SimpleDateFormat
    // -------------------------
    private void sortNotificationsNewestFirst() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            notifications.sort((n1, n2) -> {
                try {
                    Date d1 = sdf.parse(n1.getDateTime());
                    Date d2 = sdf.parse(n2.getDateTime());
                    return d2.compareTo(d1); // newest first
                } catch (ParseException e) {
                    return 0;
                }
            });

        } catch (Exception e) {
            Log.e("NotificationsActivity", "Sort failed: " + e.getMessage());
        }
    }

    private class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

        private final List<Notification> notifications;

        public NotificationAdapter(List<Notification> notifications) {
            this.notifications = notifications;
        }

        @NonNull
        @Override
        public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.notification_item, parent, false);
            return new NotificationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
            Notification notif = notifications.get(position);
            holder.title.setText(notif.getEventName());
            holder.time.setText(notif.getDateTime());
            holder.preview.setText(notif.getText());
            holder.fullText.setText(notif.getText());

            holder.itemView.setOnClickListener(v -> {
                if (holder.expandedLayout.getVisibility() == View.GONE) {
                    holder.expandedLayout.setVisibility(View.VISIBLE);
                    holder.preview.setVisibility(View.GONE);
                } else {
                    holder.expandedLayout.setVisibility(View.GONE);
                    holder.preview.setVisibility(View.VISIBLE);
                }
            });

            holder.goToEventBtn.setOnClickListener(v -> {
                controller = new EventActivityController(NotificationsActivity.this);
                controller.navigateToEventActivity(notif.getEventID(), userID);
            });
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }

        class NotificationViewHolder extends RecyclerView.ViewHolder {
            TextView title, time, preview, fullText;
            View expandedLayout;
            View goToEventBtn;

            public NotificationViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.notification_title);
                time = itemView.findViewById(R.id.notification_time);
                preview = itemView.findViewById(R.id.notification_preview);
                fullText = itemView.findViewById(R.id.notification_full_text);
                expandedLayout = itemView.findViewById(R.id.notification_expanded);
                goToEventBtn = itemView.findViewById(R.id.go_to_event_btn);
            }
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav == null) return;

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(NotificationsActivity.this, HomeActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }
            if (id == R.id.nav_profile) {
                startActivity(new Intent(NotificationsActivity.this, ProfileActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }
            if (id == R.id.nav_notifications) {
                startActivity(new Intent(NotificationsActivity.this, NotificationsActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }
            if (id == R.id.nav_search) {
                Intent intent = new Intent(NotificationsActivity.this, SearchActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            }
            if (id == R.id.nav_events) {
                startActivity(new Intent(NotificationsActivity.this, CreateEventActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }
            return false;
        });
    }
}
