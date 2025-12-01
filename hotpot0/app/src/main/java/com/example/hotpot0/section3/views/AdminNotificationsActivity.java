package com.example.hotpot0.section3.views;

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
import com.example.hotpot0.section2.controllers.EventActivityController;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Shows admin all notfications sent from organizers of events.
 */
public class AdminNotificationsActivity extends AppCompatActivity {

    private RecyclerView notificationsRecycler;
    private TextView emptyInvitationsText;
    private NotificationAdapter adapter;
    private List<Notification> notifications = new ArrayList<>();

    private EventUserLinkDB eventUserLinkDB;
    private EventActivityController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.section3_adminnotif_activity);

        notificationsRecycler = findViewById(R.id.notifications_recycler);
        emptyInvitationsText = findViewById(R.id.emptyInvitationsText);

        notificationsRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(notifications);
        notificationsRecycler.setAdapter(adapter);

        eventUserLinkDB = new EventUserLinkDB();

        fetchAllNotifications();
        setupBottomNavigation();
    }

    /**
     * Fetches ALL EventUserLinks in the database.
     * No filtering by user — returns every notification in the system.
     */
    private void fetchAllNotifications() {
        eventUserLinkDB.getAllEventUserLinks(new EventUserLinkDB.GetCallback<List<EventUserLink>>() {
            @Override
            public void onSuccess(List<EventUserLink> allLinks) {
                if (allLinks == null || allLinks.isEmpty()) {
                    emptyInvitationsText.setVisibility(View.VISIBLE);
                    return;
                }

                for (EventUserLink link : allLinks) {
                    if (link.getNotifications() != null) {
                        notifications.addAll(link.getNotifications());
                    }
                }

                if (notifications.isEmpty()) {
                    emptyInvitationsText.setVisibility(View.VISIBLE);
                } else {
                    emptyInvitationsText.setVisibility(View.GONE);
                    sortNotificationsNewestFirst();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("AdminNotif", "Failed to load EventUserLinks: " + e.getMessage());
            }
        });
    }

    /**
     * Sorts notifications by date, newest first.
     */
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
            Log.e("AdminNotificationsActivity", "Sort failed: " + e.getMessage());
        }
    }

    // Adapter (same as user version)
    private class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
        private final List<Notification> notifications;

        public NotificationAdapter(List<Notification> notifications) {
            this.notifications = notifications;
        }

        /**
         * Inflates the notification item layout.
         */
        @NonNull
        @Override
        public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.notification_item, parent, false);
            return new NotificationViewHolder(view);
        }

        /**
         * Binds notification data to the view holder.
         */
        @Override
        public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
            Notification notif = notifications.get(position);
            holder.title.setText(notif.getEventName());
            holder.time.setText(notif.getDateTime());
            holder.preview.setText(notif.getText());
            holder.fullText.setText(notif.getText());

            holder.goToEventBtn.setVisibility(View.GONE);

            holder.itemView.setOnClickListener(v -> {
                if (holder.expandedLayout.getVisibility() == View.GONE) {
                    holder.expandedLayout.setVisibility(View.VISIBLE);
                    holder.preview.setVisibility(View.GONE);
                } else {
                    holder.expandedLayout.setVisibility(View.GONE);
                    holder.preview.setVisibility(View.VISIBLE);
                }
            });

//            holder.goToEventBtn.setOnClickListener(v -> {
//                controller = new EventActivityController(AdminNotificationsActivity.this);
//                controller.navigateToEventActivity(notif.getEventID(),userID);
//            });
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }

        /**
         * ViewHolder for notification items.
         */
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
        BottomNavigationView bottomNav = findViewById(R.id.adminBottomNavigationView);
        bottomNav.setSelectedItemId(R.id.admin_notif);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.admin_notif) {
                // Already on home
                return true;
            } else if (id == R.id.admin_search) {
                Intent searchIntent = new Intent(AdminNotificationsActivity.this, AdminSearchActivity.class);
                startActivity(searchIntent);
//                overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_in_left);
                return true;
            } else if (id == R.id.admin_images) {
                startActivity(new Intent(AdminNotificationsActivity.this, AdminImageActivity.class));
//                overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_in_left);
                return true;
            } else if (id == R.id.admin_home) {
                startActivity(new Intent(AdminNotificationsActivity.this, AdminHomeActivity.class));
//                overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_in_left);
                return true;
            } else if (id == R.id.admin_settings) {
                startActivity(new Intent(AdminNotificationsActivity.this, AdminSettingsActivity.class));
//                overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_in_left);
                return true;
            }
            return false;
        });
    }
}
