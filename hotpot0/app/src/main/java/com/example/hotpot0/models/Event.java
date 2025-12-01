package com.example.hotpot0.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an event managed by an organizer and stored in Firestore.
 * <p>
 * Each {@code Event} contains metadata such as name, description, schedule, and location,
 * as well as participant lists, pricing, and registration details.
 * The class provides methods for sampling and managing participant IDs.
 * </p>
 */
public class Event {

    // Event Attributes
    private Integer eventID;
    private Integer organizerID;
    private String name, description, guidelines, location, time, startDate, endDate,
            duration, registrationStart, registrationEnd;

    private Integer capacity;
    private Double price;

    private String imageURL;
    private String qrValue;
    private Integer waitingListCapacity;

    private Boolean geolocationRequired;
    private Boolean isEventActive;
    private ArrayList<String> linkIDs;
    private ArrayList<String> sampledIDs;
    private ArrayList<String> cancelledIDs;

    /**
     * Default constructor used by Firestore for deserialization.
     * Initializes empty participant lists and default values.
     */
    public Event() {
        this.eventID = null; // Handled by Firestore in EventDB
        this.isEventActive = true;
        this.geolocationRequired = false;
        this.linkIDs = new ArrayList<>();
        this.sampledIDs = new ArrayList<>();
        this.cancelledIDs = new ArrayList<>();
    }

    public Event(Integer organizerID, String name, String description, String guidelines, String location, String time, String startDate, String endDate,
                 String duration, Integer capacity, Integer waitingListCapacity, Double price, String registrationStart, String registrationEnd,
                 String imageURL, String qrValue, Boolean geolocationRequired) {
        this.organizerID = organizerID;
        this.name = name;
        this.description = description;
        this.guidelines = guidelines;
        this.location = location;
        this.time = time;
        this.startDate = startDate;
        this.endDate = endDate;
        this.duration = duration;
        this.capacity = capacity;
        this.waitingListCapacity = waitingListCapacity;
        this.price = price;
        this.registrationStart = registrationStart;
        this.registrationEnd = registrationEnd;
        this.imageURL = imageURL;
        this.qrValue = qrValue;
        this.geolocationRequired = geolocationRequired;
        this.isEventActive = true;
        this.linkIDs = new ArrayList<>();
        this.sampledIDs = new ArrayList<>();
        this.cancelledIDs = new ArrayList<>();
    }

    // Getters and Setters
    // ==================

    /** @return the event ID assigned by Firestore
     */
    public Integer getEventID() {
        return eventID;
    }

    /** @param eventID the unique event ID assigned by Firestore */
    public void setEventID(Integer eventID) {
        this.eventID = eventID;
    }

    /** @return the ID of the event organizer */
    public Integer getOrganizerID() {
        return organizerID;
    }

    /** @param organizerID the ID of the event organizer */
    public void setOrganizerID(Integer organizerID) {
        this.organizerID = organizerID;
    }

    /** @return the name of the event */
    public String getName() {
        return name;
    }

    /** @param name the name of the event */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the event description */
    public String getDescription() {
        return description;
    }

    /** @param description a brief summary of the event */
    public void setDescription(String description) {
        this.description = description;
    }

    /** @return event participation guidelines */
    public String getGuidelines() {
        return guidelines;
    }

    /** @param guidelines event participation guidelines */
    public void setGuidelines(String guidelines) {
        this.guidelines = guidelines;
    }

    /** @return event location */
    public String getLocation() {
        return location;
    }

    /** @param location the location where the event will take place */
    public void setLocation(String location) {
        this.location = location;
    }

    /** @return time of the event */
    public String getTime() {
        return time;
    }

    /** @param time the time when the event begins */
    public void setTime(String time) {
        this.time = time;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    /** @return duration of the event */
    public String getDuration() {
        return duration;
    }

    /** @param duration the duration of the event */
    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getRegistrationStart() {
        return registrationStart;
    }

    public void setRegistrationStart(String registrationStart) {
        this.registrationStart = registrationStart;
    }

    public String getRegistrationEnd() {
        return registrationEnd;
    }

    public void setRegistrationEnd(String registrationEnd) {
        this.registrationEnd = registrationEnd;
    }

    /** @return the maximum participant capacity */
    public Integer getCapacity() {
        return capacity;
    }

    /** @param capacity the number of participants allowed */
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    /** @return the event price */
    public Double getPrice() {
        return price;
    }

    /** @param price the participation cost */
    public void setPrice(Double price) {
        this.price = price;
    }

    /** @return the event’s image URL */
    public String getImageURL() {
        return imageURL;
    }

    /** @param imageURL URL of the event image */
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    /** @return whether geolocation tracking is required */
    public Boolean getGeolocationRequired() {
        return geolocationRequired;
    }

    /** @param geolocationRequired true if geolocation verification is required */
    public void setGeolocationRequired(Boolean geolocationRequired) {
        this.geolocationRequired = geolocationRequired;
    }

    /** @return whether the event is currently active */
    public Boolean getIsEventActive() {
        return isEventActive;
    }

    /** @param eventActive true if the event is active */
    public void setIsEventActive(Boolean eventActive) {
        isEventActive = eventActive;
    }

    /** @return the list of registered participant IDs */
    public ArrayList<String> getLinkIDs() {
        return linkIDs;
    }

    /** @param linkIDs list of participant IDs registered for the event */
    public void setLinkIDs(ArrayList<String> linkIDs) {
        this.linkIDs = linkIDs;
    }

    /** @return list of sampled participant IDs */
    public ArrayList<String> getSampledIDs() {
        return sampledIDs;
    }

    /** @param sampledIDs list of participants selected for participation */
    public void setSampledIDs(ArrayList<String> sampledIDs) {
        this.sampledIDs = sampledIDs;
    }

    /** @return list of participant IDs that cancelled registration */
    public ArrayList<String> getCancelledIDs() {
        return cancelledIDs;
    }

    /** @param cancelledIDs list of participants who have cancelled */
    public void setCancelledIDs(ArrayList<String> cancelledIDs) {
        this.cancelledIDs = cancelledIDs;
    }

    public Integer getWaitingListCapacity() {
        return waitingListCapacity;
    }

    public void setWaitingListCapacity(Integer waitingListCapacity) {
        this.waitingListCapacity = waitingListCapacity;
    }

    public String getQrValue() {
        return qrValue;
    }

    public void setQrValue(String qrValue) {
        this.qrValue = qrValue;
    }

    // Utility Methods
    // ===============

    /**
     * Adds a participant ID to the list of registered participants.
     * @param linkID the unique participant ID
     * @return {@code true} if the ID was added successfully, {@code false} otherwise
     */
    public boolean addLinkID(String linkID) {
        if (linkID == null || linkID.isEmpty()) {
            return false;
        }
        if (linkIDs == null) {
            linkIDs = new ArrayList<>();
        }
        if (linkIDs.size() == capacity) {
            return false;
        }
        if (linkIDs.contains(linkID)) {
            return false; // Avoid duplicates
        }
        return linkIDs.add(linkID);
    }

    /**
     * Removes a participant ID from the list of registered participants.
     * @param linkID the participant ID to remove
     * @return {@code true} if the ID was successfully removed, {@code false} otherwise
     */
    public boolean removeLinkID(String linkID) {
        if (linkID == null || linkID.isEmpty()) {
            return false;
        }
        return linkIDs.remove(linkID);
    }

    /** @return the total number of registered participants */
    public int getTotalLinks() {
        return (linkIDs != null) ? linkIDs.size() : 0;
    }

    /** @return the total number of sampled participants */
    public int getTotalSampled() {
        return (sampledIDs != null) ? sampledIDs.size() : 0;
    }

    /** @return the total number of cancelled participants */
    public int getTotalCancelled() {
        return (cancelledIDs != null) ? cancelledIDs.size() : 0;
    }

    /**
     * Calculates the number of participants currently on the waitlist.
     * @return the total number of waitlisted participants
     */
    public Integer getTotalWaitlist() {
        return getTotalLinks() - getTotalCancelled() - 1;
    }

    /**
     * Randomly samples participants from a provided waitlist up to the event's capacity.
     * <p>
     * Existing sampled participants are cleared before adding new ones.
     * </p>
     * @param waitListParticipants the list of participants to sample from
     * @return a list of sampled participant IDs
     */
    public ArrayList<String> sampleParticipants(List<String> waitListParticipants) {
        if (waitListParticipants == null || waitListParticipants.isEmpty()) {
            return new ArrayList<>(); // Nothing to sample
        }

        int cap = this.capacity != null ? this.capacity : 0;
        int sampleSize = Math.min(cap, waitListParticipants.size());

        // Shuffle the waitlist to randomize
        List<String> shuffled = new ArrayList<>(waitListParticipants);
        Collections.shuffle(shuffled);

        // Take the first N participants as the sampled list
        List<String> sampled = shuffled.subList(0, sampleSize);

        if (this.sampledIDs == null) {
            this.sampledIDs = new ArrayList<>();
        }

        this.sampledIDs.clear(); // Replace previous sample
        this.sampledIDs.addAll(sampled);

        return this.sampledIDs;
    }

    /**
     * Fills remaining sample slots with additional participants from the waitlist
     * until the capacity is reached or the waitlist is exhausted.
     * @param waitListParticipants the list of participants to sample from
     * @return a list of newly added sampled participant IDs
     */
    public ArrayList<String> fillSampledParticipants(List<String> waitListParticipants) {
        if (waitListParticipants == null || waitListParticipants.isEmpty()) {
            return new ArrayList<>(); // Nothing to fill
        }

        int cap = this.capacity != null ? this.capacity : 0;
        if (this.sampledIDs == null) {
            this.sampledIDs = new ArrayList<>();
        }

        int spotsLeft = cap - this.sampledIDs.size();
        if (spotsLeft <= 0) {
            return new ArrayList<>(); // No spots left to fill
        }

        List<String> shuffled = new ArrayList<>(waitListParticipants);
        Collections.shuffle(shuffled);

        ArrayList<String> newlySampled = new ArrayList<>();
        for (String participant : shuffled) {
            if (newlySampled.size() >= spotsLeft) break;
            if (!this.sampledIDs.contains(participant)) {
                newlySampled.add(participant);
            }
        }

        this.sampledIDs.addAll(newlySampled);
        return newlySampled;
    }

    /**
     * Returns a human-readable summary of this event including core fields such as IDs,
     * name, schedule, capacity, pricing, image URL, geolocation flag, active status,
     * and the current registered/sampled participant IDs.
     * @return a formatted string describing this {@code Event}
     */
    @Override
    public String toString() {
        return "Event{" +
                "eventID=" + eventID +
                ", organizerID=" + organizerID +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", guidelines='" + guidelines + '\'' +
                ", location='" + location + '\'' +
                ", time='" + time + '\'' +
                ", duration='" + duration + '\'' +
                ", capacity=" + capacity +
                ", price=" + price +
                ", imageURL='" + imageURL + '\'' +
                ", geolocationRequired=" + geolocationRequired +
                ", isEventActive=" + isEventActive +
                ", linkIDs=" + linkIDs +
                ", sampledIDs=" + sampledIDs +
                '}';
    }
}