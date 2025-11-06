package com.example.hotpot0.models;

import java.util.ArrayList;
import java.util.List;

public class EventUserLink {
    private final Integer userID;
    private final Integer eventID;
    private String linkID;
    private Status status;
    private List<String> notifications;

    public EventUserLink(Integer userID, Integer eventID, String status) throws InvalidStatusException {
        this.linkID = eventID.toString() + '_' + userID.toString(); // Assigned by FireStore when the instance is saved
        this.userID = userID;
        this.eventID = eventID;
        this.status.setStatus(status);
        this.notifications = new ArrayList<>();
    }

    public EventUserLink(Integer userID, Integer eventID) throws InvalidStatusException {
        this.linkID = eventID.toString() + '_' + userID.toString(); // Assigned by FireStore when the instance is saved
        this.userID = userID;
        this.eventID = eventID;
        this.status.setStatus("inWaitList");
        this.notifications = new ArrayList<>();
    }

    public void addNotification(String notification) {
        notifications.add(notification);
    }

    public String getLinkID() {
        return linkID;
    }

    public void setLinkID(String linkID) {
        this.linkID = linkID;
    }

    public String getStatus() {
        return status.getStatus();
    }

    public void setStatus(String status) throws InvalidStatusException {
        this.status.setStatus(status);
    }

    public Integer getUserID() {
        return userID;
    }

    public Integer getEventID() {
        return eventID;
    }

    public List<String> getNotifications() {
        return notifications;
    }

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
