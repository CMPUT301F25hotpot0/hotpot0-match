package com.example.hotpot0.models;

import java.util.ArrayList;
import java.util.List;

public class EventUserLink {
    private final Integer userID;
    private final Integer eventID;
    private Integer linkID;
    private Status status;
    private List<String> notifications;

    public EventUserLink(Integer userID, Integer eventID, String status) throws InvalidStatusException {
        this.linkID = null; // Assigned by FireStore when the instance is saved
        this.userID = userID;
        this.eventID = eventID;
        this.status.setStatus(status);
        this.notifications = new ArrayList<>();
    }

    public EventUserLink(Integer userID, Integer eventID) throws InvalidStatusException {
        this.linkID = null; // Assigned by FireStore when the instance is saved
        this.userID = userID;
        this.eventID = eventID;
        this.status.setStatus("inWaitList");
        this.notifications = new ArrayList<>();
    }

    public void addNotification(String notification) {
        notifications.add(notification);
    }

    public Integer getLinkID() {
        return linkID;
    }

    public void setLinkID(Integer linkID) {
        this.linkID = linkID;
    }
}
