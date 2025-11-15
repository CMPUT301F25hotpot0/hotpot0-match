package com.example.hotpot0.section2.controllers;

import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import android.text.TextUtils;
import android.content.Context;

import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.EventDB;
import com.example.hotpot0.models.PicturesDB;
import com.example.hotpot0.models.ProfileDB;
import com.example.hotpot0.models.UserProfile;
import com.example.hotpot0.models.EventUserLink;
import com.example.hotpot0.models.EventUserLinkDB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

/**
 * Handles the creation of new events in the system.
 * <p>
 * This class performs validation on event details, creates a new {@link Event},
 * links it to the organizer through {@link EventUserLink}, and updates the organizer’s profile.
 * </p>
 */
public class CreateEventHandler {

    private ProfileDB profileDB;
    private final EventDB eventDB;
    private final Context context;
    private EventUserLinkDB eventUserLinkDB;

    /**
     * Constructs a new {@code CreateEventHandler}.
     *
     * @param context the Android context used for database access and UI feedback.
     */
    public CreateEventHandler(Context context) {
        this.context = context;
        this.eventDB = new EventDB();
        this.eventUserLinkDB = new EventUserLinkDB();
        this.profileDB = new ProfileDB();
    }

    public Context getContext() {
        return context;
    }

    public void createEvent(Integer organizerID,
                            String name,
                            String description,
                            String guidelines,
                            String location,
                            String time,
                            String startDate,
                            String endDate,
                            String duration,
                            Integer capacity,
                            Integer waitingListCapacity,
                            Double price,
                            String regStart,
                            String regEnd,
                            String imageUriString,
                            Boolean geolocationRequired,
                            EventDB.GetCallback<Event> callback) {

        Uri imageUri = imageUriString != null ? Uri.parse(imageUriString) : null;

        // Create event object
        Event event = new Event(
                organizerID,
                name,
                description,
                guidelines,
                location,
                time,
                startDate,
                endDate,
                duration,
                capacity,
                waitingListCapacity,
                price,
                regStart,
                regEnd,
                null,         // imageURL is null initially
                null,                  // qrValue is null initially
                geolocationRequired
        );

        eventDB.addEvent(event, new EventDB.GetCallback<Event>() {
            @Override
            public void onSuccess(Event result) {

                int eventID = result.getEventID();

                // If image exists → upload it
                if (imageUriString != null && !imageUriString.isEmpty()) {

                    PicturesDB picturesDB = new PicturesDB();

                    Uri localUri = getSafeUriForUpload(imageUri);

                    if (localUri != null) {

                        picturesDB.uploadEventImage(localUri, eventID, new PicturesDB.Callback<String>() {
                            @Override
                            public void onSuccess(String downloadURL) {

                                // ONLY update imageURL (QR is already set)
                                eventDB.updateEventImageURL(result, downloadURL, new EventDB.GetCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        finalizeEventCreation(organizerID, result, callback);
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Toast.makeText(context, "EventDB.updateEventImageURL failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        callback.onFailure(e);
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(context, "PicturesDB.uploadEventImage failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                callback.onFailure(e);
                            }
                        });

                    } else {
                        Toast.makeText(context, "Failed to prepare image for upload", Toast.LENGTH_LONG).show();
                        finalizeEventCreation(organizerID, result, callback);
                    }

                } else {
                    // No image → finish immediately
                    finalizeEventCreation(organizerID, result, callback);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(context, "EventDB.addEvent failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                callback.onFailure(e);
            }
        });
    }

    private void finalizeEventCreation(int organizerID, Event event, EventDB.GetCallback<Event> callback) {
        int eventID = event.getEventID();

        EventUserLink link = new EventUserLink(organizerID, eventID, "Organizer");

        eventUserLinkDB.addEventUserLink(link, new EventUserLinkDB.GetCallback<EventUserLink>() {
            @Override
            public void onSuccess(EventUserLink linkResult) {
                profileDB.getUserByID(organizerID, new ProfileDB.GetCallback<UserProfile>() {
                    @Override
                    public void onSuccess(UserProfile userProfile) {

                        profileDB.addLinkIDToUser(userProfile, linkResult.getLinkID(), new ProfileDB.GetCallback<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                eventDB.addLinkIDToEvent(event, linkResult.getLinkID(), new EventDB.GetCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        callback.onSuccess(event);
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        callback.onFailure(e);
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                callback.onFailure(e);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    private Uri getSafeUriForUpload(Uri originalUri) {
        if (originalUri == null) return null;

        try {
            Context context = getContext();
            if (context == null) {
                Log.e("CreateEventHandler", "Context is null");
                return null;
            }

            InputStream in = context.getContentResolver().openInputStream(originalUri);
            if (in == null) {
                Log.e("CreateEventHandler", "Cannot open input stream from URI: " + originalUri);
                return null;
            }

            // Use cache directory
            File cacheDir = context.getCacheDir();
            File tempFile = new File(cacheDir, "event_upload_" + System.currentTimeMillis() + ".png");

            OutputStream out = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();

            Log.d("CreateEventHandler", "Temp file created: " + tempFile.getAbsolutePath());
            return Uri.fromFile(tempFile);

        } catch (Exception e) {
            Log.e("CreateEventHandler", "Error creating safe URI", e);
            return null;
        }
    }
}
