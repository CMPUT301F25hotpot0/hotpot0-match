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

    /**
     * Required empty constructor for Firestore deserialization.*/
    public UserProfile() {}

    /**
     * Constructs a new UserProfile using provided user details and
     * automatically assigns a device ID via {@link Settings.Secure#ANDROID_ID}.
     *
     *
     * @param context      Android application context
     * @param name         user's name
     * @param emailID      user's email address
     * @param phoneNumber  user's phone number
     */
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
        this.latitude = 37.7749;
        this.longitude = -122.4194;
        this.linkIDs = new ArrayList<>();
    }

    // Getters and Setters

    /**
     * Returns the Firestore document ID of the user.
     *
     * @return the unique user ID
     */
    public Integer getUserID() { return userID; }

    /**
     * Sets the Firestore document ID for this user.
     *
     * @param userID the unique ID to assign
     */
    public void setUserID(Integer userID) { this.userID = userID; }

    /**
     * Returns the unique device ID associated with the user.
     *
     * @return the device ID string
     */
    public String getDeviceID() { return deviceID; }

    /**
     * Sets the unique device ID for this user.
     *
     * @param deviceID the Android device identifier
     */
    public void setDeviceID(String deviceID) { this.deviceID = deviceID; }

    /**
     * Returns the user's name.
     *
     * @return user's name
     */
    public String getName() { return name; }

    /**
     * Sets the user's name.
     *
     * @param name user's full name
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns the user's email address.
     *
     * @return user's email
     */
    public String getEmailID() { return emailID; }

    /**
     * Sets the user's email address.
     *
     * @param emailID email address to assign
     */
    public void setEmailID(String emailID) { this.emailID = emailID; }

    /**
     * Returns the user's phone number.
     *
     * @return user's phone number
     */
    public String getPhoneNumber() { return phoneNumber; }

    /**
     * Sets the user's phone number.
     *
     * @param phoneNumber user's contact number
     */
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    /**
     * Returns whether the user has enabled notifications.
     *
     * @return true if notifications are enabled, false otherwise
     */
    public Boolean getNotificationsEnabled() { return notificationsEnabled; }

    /**
     * Updates the user's notification preference.
     *
     * @param notificationsEnabled true to enable notifications; false to disable
     */
    public void setNotificationsEnabled(Boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }

    /**
     * Returns the user's last known latitude.
     *
     * @return latitude
     */
    public Double getLatitude() { return latitude; }

    /**
     * Sets the user's latitude coordinate.
     *
     * @param latitude geographic latitude
     */
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    /**
     * Returns the user's last known longitude.
     *
     * @return longitude
     */
    public Double getLongitude() { return longitude; }

    /**
     * Sets the user's longitude coordinate.
     *
     * @param longitude geographic longitude
     */
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    /**
     * Returns a list of EventUserLink IDs associated with this user.
     *
     * @return list of linked event IDs
     */
    public ArrayList<String> getLinkIDs() { return linkIDs; }

    /**
     * Sets the list of EventUserLink IDs for this user.
     *
     * @param linkIDs list of link IDs to assign
     */
    public void setLinkIDs(ArrayList<String> linkIDs) {
        this.linkIDs = linkIDs;
    }

    // Utility

    /**
     * Adds a new EventUserLink ID to this user's profile.
     *
     * @param linkID the event link ID to add
     */
    public void addLinkID(String linkID) {
        linkIDs.add(linkID);
    }

    /**
     * Removes an EventUserLink ID from this user's profile by index.
     *
     * @param linkID index or value to remove from the list
     */
    public void removeLinkID(int linkID) {
        linkIDs.remove(linkID);
    }

    /**
     * Returns a string representation of this UserProfile for debugging and logging.
     *
     * @return a formatted string containing all user profile fields
     */
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