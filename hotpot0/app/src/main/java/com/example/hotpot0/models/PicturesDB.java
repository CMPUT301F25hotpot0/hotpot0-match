package com.example.hotpot0.models;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class PicturesDB {

    private final StorageReference storageRef;

    public PicturesDB() {
        storageRef = FirebaseStorage.getInstance().getReference();
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    // Upload a new event image
    public void uploadEventImage(Uri imageUri, int eventID, Callback<String> callback) {
        if (imageUri == null) {
            callback.onFailure(new Exception("Image URI is null"));
            return;
        }

        Log.d("PicturesDB", "Uploading image for event " + eventID + " from URI: " + imageUri.toString());

        // Check if the file exists for file URIs
        if ("file".equals(imageUri.getScheme())) {
            File file = new File(imageUri.getPath());
            if (!file.exists()) {
                callback.onFailure(new Exception("File does not exist: " + file.getAbsolutePath()));
                return;
            }
            Log.d("PicturesDB", "File exists, size: " + file.length());
        }

        String filename = "event-" + eventID + ".png";
        StorageReference imageRef = storageRef.child("event_images/" + filename);

        Log.d("PicturesDB", "Firebase Storage path: " + imageRef.getPath());

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d("PicturesDB", "Image upload successful for event " + eventID);
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Log.d("PicturesDB", "Download URL obtained: " + uri.toString());
                        callback.onSuccess(uri.toString());
                    }).addOnFailureListener(e -> {
                        Log.e("PicturesDB", "Failed to get download URL", e);
                        callback.onFailure(e);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("PicturesDB", "Image upload failed for event " + eventID, e);
                    callback.onFailure(e);
                })
                .addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    Log.d("PicturesDB", "Upload progress: " + progress + "%");
                });
    }

    // Overwrite event image
    public void updateEventImage(Uri newImageUri, int eventID, Callback<String> callback) {
        uploadEventImage(newImageUri, eventID, callback);
    }

    // Delete event image
    public void deleteEventImage(int eventID, Callback<Void> callback) {
        StorageReference imageRef = storageRef.child("event_images/" + "event-" + eventID + ".png");
        imageRef.delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }
}