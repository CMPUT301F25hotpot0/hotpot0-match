package com.example.hotpot0.models;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


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
//    public void deleteEventImage(int eventID, Callback<Void> callback) {
//        StorageReference imageRef = storageRef.child("event_images/" + "event-" + eventID + ".png");
//        imageRef.delete()
//                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
//                .addOnFailureListener(callback::onFailure);
//    }

    public void deleteEventImage(int eventId, Callback<Void> callback) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Path to storage file
        StorageReference imageRef = storage.getReference().child("event-images/event-" + eventId + ".png");

        // Delete from storage
        imageRef.delete().addOnSuccessListener(aVoid -> {

            // After storage delete, remove URL from Firestore
            db.collection("Events")
                    .document(String.valueOf(eventId))
                    .update("imageURL", FieldValue.delete())
                    .addOnSuccessListener(a -> callback.onSuccess(null))
                    .addOnFailureListener(callback::onFailure);

        }).addOnFailureListener(callback::onFailure);
    }


    public void getAllEventImages(Callback<List<String>> callback) {
        StorageReference eventsRef = storageRef.child("event_images/");

        eventsRef.listAll()
                .addOnSuccessListener(listResult -> {
                    List<String> urls = new ArrayList<>();

                    if (listResult.getItems().isEmpty()) {
                        callback.onSuccess(urls);
                        return;
                    }

                    for (StorageReference item : listResult.getItems()) {
                        item.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    urls.add(uri.toString());

                                    if (urls.size() == listResult.getItems().size()) {
                                        callback.onSuccess(urls);
                                    }
                                })
                                .addOnFailureListener(callback::onFailure);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }


}