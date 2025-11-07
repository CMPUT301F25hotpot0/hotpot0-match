package com.example.hotpot0.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Event {
    private Integer eventID;
    private Integer organizerID;
    private String name, description, guidelines, location, time, date,
            duration, registration_period;
    private Integer capacity;
    private Double price;
    private String imageURL;
    private Boolean geolocationRequired;
    private Boolean isEventActive;
    private ArrayList<String> linkIDs;
    private ArrayList<String> sampledIDs;
    private ArrayList<String> cancelledIDs;

    public Event() {
        this.eventID = null; // Handled by Firestore in EventDB
        this.linkIDs = new ArrayList<>();
        this.sampledIDs = new ArrayList<>();
        this.isEventActive = true;
        this.geolocationRequired = false;
        this.sampledIDs = new ArrayList<>();
        this.cancelledIDs = new ArrayList<>();
    }

    public Event(Integer organizerID, String name, String description, String guidelines,
                 String location, String time, String date, String duration,
                 Integer capacity, Double price, String registration_period,
                 String imageURL, Boolean geolocationRequired) {
        this.eventID = null; // Handled by Firestore in EventDB
        this.organizerID = organizerID;
        this.name = name;
        this.description = description;
        this.guidelines = guidelines;
        this.location = location;
        this.time = time;
        this.date = date;
        this.duration = duration;
        this.capacity = capacity;
        this.price = price;
        this.registration_period = registration_period;
        this.imageURL = imageURL;
        this.geolocationRequired = geolocationRequired;
        this.isEventActive = true;
        this.linkIDs = new ArrayList<>();
        this.sampledIDs = new ArrayList<>();
        this.cancelledIDs = new ArrayList<>();
    }

    public Integer getEventID() {
        return eventID;
    }

    public void setEventID(Integer eventID) {
        this.eventID = eventID;
    }

    public Integer getOrganizerID() {
        return organizerID;
    }

    public void setOrganizerID(Integer organizerID) {
        this.organizerID = organizerID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGuidelines() {
        return guidelines;
    }

    public void setGuidelines(String guidelines) {
        this.guidelines = guidelines;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getRegistration_period() {
        return registration_period;
    }

    public void setRegistration_period(String registration_period) {
        this.registration_period = registration_period;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public Boolean getGeolocationRequired() {
        return geolocationRequired;
    }

    public void setGeolocationRequired(Boolean geolocationRequired) {
        this.geolocationRequired = geolocationRequired;
    }

    public Boolean getIsEventActive() {
        return isEventActive;
    }

    public void setIsEventActive(Boolean eventActive) {
        isEventActive = eventActive;
    }

    public ArrayList<String> getLinkIDs() {
        return linkIDs;
    }

    public void setLinkIDs(ArrayList<String> linkIDs) {
        this.linkIDs = linkIDs;
    }

    public ArrayList<String> getSampledIDs() {
        return sampledIDs;
    }

    public void setSampledIDs(ArrayList<String> sampledIDs) {
        this.sampledIDs = sampledIDs;
    }

    public ArrayList<String> getCancelledIDs() {
        return cancelledIDs;
    }

    public void setCancelledIDs(ArrayList<String> cancelledIDs) {
        this.cancelledIDs = cancelledIDs;
    }

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

    public boolean removeLinkID(String linkID) {
        if (linkID == null || linkID.isEmpty()) {
            return false;
        }
        return linkIDs.remove(linkID);
    }

    public int getTotalLinks() {
        return (linkIDs != null) ? linkIDs.size() : 0;
    }

    public int getTotalSampled() {
        return (sampledIDs != null) ? sampledIDs.size() : 0;
    }

    public int getTotalCancelled() {
        return (cancelledIDs != null) ? cancelledIDs.size() : 0;
    }

    public int getTotalWaitlist() {
        return getTotalLinks();
    }

    public String toString() {
        return "Event{" +
                "eventID=" + eventID +
                ", organizerID=" + organizerID +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", guidelines='" + guidelines + '\'' +
                ", location='" + location + '\'' +
                ", time='" + time + '\'' +
                ", date='" + date + '\'' +
                ", duration='" + duration + '\'' +
                ", registration_period='" + registration_period + '\'' +
                ", capacity=" + capacity +
                ", price=" + price +
                ", imageURL='" + imageURL + '\'' +
                ", geolocationRequired=" + geolocationRequired +
                ", isEventActive=" + isEventActive +
                ", linkIDs=" + linkIDs +
                ", sampledIDs=" + sampledIDs +
                '}';
    }

//    public ArrayList<String> sampleParticipants(List<String> waitListParticipants) {
//        if (linkIDs == null || linkIDs.isEmpty()) {
//            throw new IllegalStateException("No participants to sample from.");
//        }
//        int cap = this.capacity != null ? this.capacity : 0;
//        int sampleSize = Math.min(cap, waitListParticipants.size());
//        ArrayList<String> randomized = new ArrayList<>();
//        Collections.shuffle(waitListParticipants);
//
//        List<String> sampled = waitListParticipants.subList(0, sampleSize);
//        this.sampledIDs = new ArrayList<>(sampled);
//        return this.sampledIDs;
//    }
//
//    public ArrayList<String> fillSampledParticipants(List<String> waitListParticipants) {
//        if (linkIDs == null || linkIDs.isEmpty()) {
//            throw new IllegalStateException("No participants to sample from.");
//        }
//        int cap = this.capacity != null ? this.capacity : 0;
//        int currentSampledSize = this.sampledIDs != null ? this.sampledIDs.size() : 0;
//        int spotsLeft = cap - currentSampledSize;
//        if (spotsLeft <= 0) {
//            return new ArrayList<>(); // No spots left to fill
//        }
//
//        ArrayList<String> randomized = new ArrayList<>(waitListParticipants);
//        Collections.shuffle(randomized);
//
//        ArrayList<String> newlySampled = new ArrayList<>();
//        for (String participant : randomized) {
//            if (newlySampled.size() >= spotsLeft) {
//                break;
//            }
//            if (!this.sampledIDs.contains(participant)) {
//                newlySampled.add(participant);
//            }
//        }
//
//        if (this.sampledIDs == null) {
//            this.sampledIDs = new ArrayList<>();
//        }
//        this.sampledIDs.addAll(newlySampled);
//        return newlySampled;
//    }

    /**
     * Randomly samples participants from the provided waitlist.
     * The number of samples is limited by the event's capacity.
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
     * Fills remaining sampled spots with additional participants from the waitlist.
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
}
