package com.example.hotpot0.models;

import java.util.ArrayList;
import java.util.List;

public class Status {
    private String status;
    private final List<String> statuses;

    /**
     * Constructs a Status object with a predefined list of valid statuses.
     * <p>
     * The {@code Status} class defines a fixed set of allowed statuses that describe
     * the relationship between a user and an event, like waitlisted, accepted, cancelled, etc.
     * Attempting to assign a status not present in this predefined list will throw
     * an IllegalArgumentException.
     * </p>
     */
    public Status() {
        statuses = new ArrayList<>();

        // Fixed allowed statuses
        statuses.add("inWaitList");
        statuses.add("Accepted");
        statuses.add("Declined");
        statuses.add("Organizer");
//        statuses.add("OrganizerPreSample");
        statuses.add("Sampled");
        statuses.add("Cancelled");
//        statuses.add("OrganizerPostSample");
    }

    /**
     * Sets the current status to the provided status if it is valid.
     *
     * @param status The status to set. It must be one of the predefined valid statuses.
     */
    public void setStatus(String status) {
        if (statuses.contains(status)) {
            this.status = status;
        }
        else {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    /**
     * Gets the current status.
     *
     * @return The current status.
     */
    public String getStatus() {
        return status;
    }
}
