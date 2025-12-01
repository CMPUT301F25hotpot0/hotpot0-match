package com.example.hotpot0.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Notification class representing notifications for event invitations.
 */
public class Notification {

    // Key: status string, Value: notification text
    public static final Map<String, String> NOTIF_TEXTS = new HashMap<>();

    static {
        NOTIF_TEXTS.put("Sampled",
                "Congratulations! You have been chosen to attend the event '[Event Name]'. Please confirm your attendance for the event by accepting/declining your invitation.");

        NOTIF_TEXTS.put("inWaitList",
                "Sorry! You have lost the lottery of attending the event '[Event Name]'. Fret not - you are still part of the waiting list and have a chance of being chosen again if slots become available. If you wish to leave the waitlist, you can do so from the event page.");

        NOTIF_TEXTS.put("Cancelled",
                "Sorry! You can no longer accept the invitation for the event '[Event Name]'.");
        NOTIF_TEXTS.put("Accepted",
                "Congratulations! Your attendance in the event '[Event Name]' has been confirmed. Have fun, lucky winner.");
    }

    private String eventName;
    private String dateTime;
    private Status status;  // You already have this
    private String text;
    private boolean customNotif;
    private boolean isResampledNotif;
    private Integer eventID;

    /** Empty constructor */
    public Notification() {}

    /** Automatic Notification
     *
     * @param dateTime
     * @param status
     * @param eventName
     * @param eventID
     */
    public Notification(String dateTime, Status status, String eventName, Integer eventID) {
        this.eventID = eventID;
        this.eventName = eventName;
        this.dateTime = dateTime;
        this.status = status;
        this.customNotif = false;
        this.isResampledNotif = false;

        // Get text from static dictionary
        this.text = resolveAutomaticText(status, eventName);
    }

    /** Custom Notification
     *
     * @param dateTime
     * @param status
     * @param text
     * @param eventName
     * @param eventID
     * @param customNotif
     */
    public Notification(String dateTime, Status status, String text, String eventName, Integer eventID, boolean customNotif) {
        this.eventID = eventID;
        this.eventName = eventName;
        this.dateTime = dateTime;
        this.status = status;
        this.text = text;
        this.customNotif = customNotif;
        this.isResampledNotif = false;
    }

    /** Resampled Notification
     *
     * @param dateTime
     * @param status
     * @param isResampledNotif
     * @param eventName
     * @param eventID
     */
    public Notification(String dateTime, Status status, boolean isResampledNotif, String eventName, Integer eventID) {
        this.eventID = eventID;
        this.eventName = eventName;
        this.dateTime = dateTime;
        this.status = status;
        this.customNotif = false;
        this.isResampledNotif = isResampledNotif;

        this.text = resolveAutomaticText(status, eventName);
    }

    /** Resolves the automatic notification text based on status and event name.
     *
     * @param status The status of the notification.
     * @param eventName The name of the event.
     * @return The resolved notification text.
     */
    private String resolveAutomaticText(Status status, String eventName) {
        String base = NOTIF_TEXTS.get(status.getStatus());
        if (base == null) return null;
        if (eventName == null) eventName = "Event"; // Default
        return base.replace("[Event Name]", eventName);
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Integer getEventID() {
        return eventID;
    }

    public void setEventID(Integer eventID) {
        this.eventID = eventID;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isCustomNotif() {
        return customNotif;
    }

    public void setCustomNotif(boolean customNotif) {
        this.customNotif = customNotif;
    }

    public boolean isResampledNotif() {
        return isResampledNotif;
    }

    public void setResampledNotif(boolean resampledNotif) {
        isResampledNotif = resampledNotif;
    }
}
