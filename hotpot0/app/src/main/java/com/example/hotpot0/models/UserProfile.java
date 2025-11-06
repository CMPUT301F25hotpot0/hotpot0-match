package com.example.hotpot0.models;

import java.util.ArrayList;
import android.content.Context;
import android.provider.Settings;
import androidx.annotation.NonNull;
import android.annotation.SuppressLint;


/**
 * Represents a user profile stored in Firestore.
 * Firestore handles ID generation unless you manually assign one.
 */
public class UserProfile {

    // Firestore document fields

    private Integer userID;               // Firestore document ID
    private String deviceID;              // Device-specific identifier
    private String name;                  // User's name
    private String emailID;               // User's email
    private String phoneNumber;           // User's phone number
    private Boolean notificationsEnabled; // User's notification preference
    private Double latitude;              // User's current latitude
    private Double longitude;             // User's current longitude
    private ArrayList<String> linkIDs;    // User's EventUserLink IDs

    // Constructors

    // Required empty constructor for Firestore
    public UserProfile() {}

    @SuppressLint("HardwareIds")
    public UserProfile(Context context, String name, String emailID, String phoneNumber) {
        this.userID = null; // Firestore will assign it when User is being saved in Database
        this.deviceID = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        this.name = name;
        this.emailID = emailID;
        this.phoneNumber = phoneNumber;
        this.notificationsEnabled = true;
        this.latitude = null;
        this.longitude = null;
        this.linkIDs = new ArrayList<>();
    }

    // Getters and Setters

    public Integer getUserID() { return userID; }
    public void setUserID(Integer userID) { this.userID = userID; }

    public String getDeviceID() { return deviceID; }
    public void setDeviceID(String deviceID) { this.deviceID = deviceID; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmailID() { return emailID; }
    public void setEmailID(String emailID) { this.emailID = emailID; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Boolean getNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(Boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public ArrayList<String> getLinkIDs() { return linkIDs; }
    public void setLinkIDs(ArrayList<String> linkIDs) {
        this.linkIDs = linkIDs;
    }

    // Utility

    public void addLinkID(String linkID) {
        linkIDs.add(linkID);
    }

    public void removeLinkID(int linkID) {
        linkIDs.remove(linkID);
    }

    @NonNull
    @Override
    public String toString() {
        return "UserProfile{" +
                "userID='" + userID + '\'' +
                ", deviceID='" + deviceID + '\'' +
                ", name='" + name + '\'' +
                ", emailID='" + emailID + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", notificationsEnabled=" + notificationsEnabled +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", linkIDs=" + linkIDs +
                '}';
    }
}