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
        statuses.add("Sampled");
        statuses.add("Cancelled");
    }

    /**
     * Sets the current status to the provided status if it is valid.
     *
     * @param status The status to set. It must be one of the predefined valid statuses.
     * @throws InvalidStatusException If the provided status is not valid.
     */
    public void setStatus(String status) throws InvalidStatusException {
        if (statuses.contains(status)) {
            this.status = status;
        }
        else{
            throw new InvalidStatusException("Invalid status: " + status);
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
