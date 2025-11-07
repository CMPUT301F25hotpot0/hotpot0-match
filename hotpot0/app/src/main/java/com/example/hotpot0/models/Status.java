package com.example.hotpot0.models;

import java.util.ArrayList;
import java.util.List;

public class Status {
    private String status;
    private final List<String> statuses;

    /**
     * Constructs a Status object with a predefined list of valid statuses.
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
