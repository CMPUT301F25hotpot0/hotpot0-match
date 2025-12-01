package com.example.hotpot0.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the relationship between a user and an event in Firestore.
 * <p>
 * Each {@code EventUserLink} connects a specific user to a specific event
 * and stores their registration status (e.g., inWaitList, confirmed, cancelled)
 * as well as notifications related to that link.
 * </p>
 */
public class EventUserLink {

    // EventUserLink attributes
    private Integer userID;
    private Integer eventID;
    private String linkID;
    private Status status = new Status();
    private ArrayList<Notification> notifications;
    private Double latitude;
    private Double longitude;

    //

    /**
     * Default constructor required for Firestore deserialization.
     */
    public EventUserLink() {}

    /**
     * Constructs a new {@code EventUserLink} with a given user, event, and status.
     * @param userID the user’s unique ID
     * @param eventID the event’s unique ID
     * @param status the participation status (e.g., "confirmed", "inWaitList", "cancelled")
     */
    public EventUserLink(Integer userID, Integer eventID, String status) {
        this.linkID = eventID.toString() + '_' + userID.toString(); // Assigned by FireStore when the instance is saved
        this.userID = userID;
        this.eventID = eventID;
        this.status.setStatus(status);
        this.notifications = new ArrayList<>();
    }

    /**
     * Constructs a new {@code EventUserLink} with a default status of {@code "inWaitList"}.
     * @param userID  the user’s unique ID
     * @param eventID the event’s unique ID
     */
    public EventUserLink(Integer userID, Integer eventID, Double latitude, Double longitude) {
        this.linkID = eventID.toString() + '_' + userID.toString(); // Assigned by FireStore when the instance is saved
        this.userID = userID;
        this.eventID = eventID;
        this.status.setStatus("inWaitList");
        this.notifications = new ArrayList<>();
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters
    // =================

    /**
     * Retrieves the link ID, which uniquely identifies the user-event association.
     * @return the unique link ID string
     */
    public String getLinkID() {
        return linkID;
    }

    /**
     * Sets the unique link ID.
     * @param linkID the new link ID to assign
     */
    public void setLinkID(String linkID) {
        this.linkID = linkID;
    }

    /**
     * Retrieves the user’s participation status for this event.
     * @return the current status string (e.g., "confirmed", "cancelled")
     */
    public String getStatus() {
        return status.getStatus();
    }

    /**
     * Updates the user’s participation status for this event.
     * @param status the new status string
     */
    public void setStatus(String status) {
        this.status.setStatus(status);
    }

    /**
     * Retrieves the user ID associated with this link.
     * @return the user ID
     */
    public Integer getUserID() {
        return userID;
    }

    /**
     * Retrieves the event ID associated with this link.
     * @return the event ID
     */
    public Integer getEventID() {
        return eventID;
    }

    /**
     * Retrieves the list of notifications sent to the user for this event.
     * @return a list of notification messages
     */
    public ArrayList<Notification> getNotifications() {
        return notifications;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }


    // Utility Methods
    // ===============

    /**
     * Adds a new notification message to this event-user link.
     * @param notification the notification message to add
     */
    public void addNotification(Notification notification) {
        notifications.add(notification);
    }

    /**
     * Returns a formatted string representation of this {@code EventUserLink}.
     * Includes the user ID, event ID, link ID, current status, and notifications.
     * @return a string representation of this link
     */
    @Override
    public String toString() {
        return "EventUserLink{" +
                "userID=" + userID +
                ", eventID=" + eventID +
                ", linkID='" + linkID + '\'' +
                ", status='" + status.getStatus() + '\'' +
                ", notifications=" + notifications +
                '}';
    }
}
