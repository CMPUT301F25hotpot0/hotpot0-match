package com.example.hotpot0;

import com.example.hotpot0.models.Event;
import com.example.hotpot0.models.Status;
import com.example.hotpot0.models.Notification;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class EventTest {

    @Test
    public void testDefaultConstructor() {
        Event event = new Event();

        assertTrue(event.getIsEventActive());
        assertTrue(event.getJoinable());
        assertFalse(event.getGeolocationRequired());

        assertNotNull(event.getLinkIDs());
        assertNotNull(event.getSampledIDs());
        assertNotNull(event.getCancelledIDs());
        assertNotNull(event.getNotifications());
    }

    @Test
    public void testParameterizedConstructor() {
        Event event = new Event(
                1, "Party", "Desc", "Guide", "Location",
                "10:00", "2025-01-01", "2025-01-02",
                "2h", 10, 5, 20.0,
                "2025-01-01", "2025-01-02",
                "img.png", "qrValue", true
        );

        assertEquals(Integer.valueOf(1), event.getOrganizerID());
        assertEquals("Party", event.getName());
        assertEquals("Location", event.getLocation());
        assertEquals(Integer.valueOf(10), event.getCapacity());
        assertEquals(Integer.valueOf(5), event.getWaitingListCapacity());
        assertEquals(Double.valueOf(20.0), event.getPrice());
        assertTrue(event.getGeolocationRequired());
    }

    @Test
    public void testAddLinkID() {
        Event event = new Event();
        event.setCapacity(2);

        assertTrue(event.addLinkID("u1"));
        assertTrue(event.addLinkID("u2"));

        assertFalse(event.addLinkID("u2"));  // duplicate
        assertFalse(event.addLinkID(""));    // empty
        assertFalse(event.addLinkID(null));  // null
        assertFalse(event.addLinkID("u3"));  // at capacity

        assertEquals(2, (int) event.getTotalLinks());
    }

    @Test
    public void testRemoveLinkID() {
        Event event = new Event();
        event.setCapacity(5);

        event.addLinkID("u1");
        event.addLinkID("u2");

        assertTrue(event.removeLinkID("u1"));
        assertFalse(event.removeLinkID("notThere"));
        assertFalse(event.removeLinkID(""));

        assertEquals(1, (int) event.getTotalLinks());
    }

    @Test
    public void testTotalWaitlist() {
        Event event = new Event();
        event.setCapacity(5);

        event.addLinkID("u1");
        event.addLinkID("u2");
        event.getCancelledIDs().add("u1");

        // totalWaitlist = totalLinks - cancelled - 1
        assertEquals(Integer.valueOf(0), event.getTotalWaitlist());
    }

    @Test
    public void testSampleParticipants() {
        Event event = new Event();
        event.setCapacity(2);
        event.setEventID(99);
        event.setName("MegaEvent");

        List<String> waitlist = Arrays.asList("a", "b", "c");

        ArrayList<String> sampled = event.sampleParticipants(waitlist);

        assertEquals(2, sampled.size());
        assertTrue(waitlist.containsAll(sampled)); // sampled users come from waitlist

        // Notifications should be generated
        assertEquals(1, event.getNotifications().size());
        Notification notif = event.getNotifications().get(0);

        assertEquals("MegaEvent", notif.getEventName());
        assertEquals(Integer.valueOf(99), notif.getEventID());
        assertEquals("Sampled", notif.getStatus().getStatus());
        assertTrue(notif.isResampledNotif()); // sample notifications use this constructor
    }

    @Test
    public void testFillSampledParticipants() {
        Event event = new Event();
        event.setCapacity(3);

        event.getSampledIDs().add("x"); // already sampled one

        List<String> waitlist = Arrays.asList("a", "b", "c");

        ArrayList<String> filled = event.fillSampledParticipants(waitlist);

        assertEquals(2, filled.size()); // 3 capacity - 1 existing

        // Should not have duplicates
        assertFalse(filled.contains("x"));
        assertTrue(waitlist.containsAll(filled));

        // Notifications added
        assertEquals(1, event.getNotifications().size());
    }

    @Test
    public void testAddNotification() {
        Event event = new Event();
        event.setName("Birthday");
        event.setEventID(33);

        Status status = new Status();
        status.setStatus("Accepted");

        event.addNotification(status, new ArrayList<>());

        assertEquals(1, event.getNotifications().size());
        Notification notif = event.getNotifications().get(0);

        assertEquals("Birthday", notif.getEventName());
        assertEquals(Integer.valueOf(33), notif.getEventID());
        assertEquals("Accepted", notif.getStatus().getStatus());
    }

    @Test
    public void testAddCustomNotification() {
        Event event = new Event();
        event.setName("Expo");
        event.setEventID(44);

        Status status = new Status();
        status.setStatus("Cancelled");

        event.addCustomNotification(status, "Custom alert!", new ArrayList<>());

        Notification notif = event.getNotifications().get(0);

        assertEquals("Custom alert!", notif.getText());
        assertTrue(notif.isCustomNotif());
    }

    @Test
    public void testSettersAndGetters() {
        Event event = new Event();

        event.setEventID(10);
        event.setOrganizerID(20);
        event.setName("N1");
        event.setDescription("D1");
        event.setGuidelines("G1");
        event.setLocation("L1");
        event.setTime("10:00");
        event.setDuration("2h");
        event.setCapacity(50);
        event.setPrice(10.5);
        event.setImageURL("img");
        event.setGeolocationRequired(true);
        event.setIsEventActive(false);
        event.setJoinable(false);

        assertEquals(Integer.valueOf(10), event.getEventID());
        assertEquals(Integer.valueOf(20), event.getOrganizerID());
        assertEquals("N1", event.getName());
        assertEquals("D1", event.getDescription());
        assertEquals("G1", event.getGuidelines());
        assertEquals("L1", event.getLocation());
        assertEquals("10:00", event.getTime());
        assertEquals("2h", event.getDuration());
        assertEquals(Integer.valueOf(50), event.getCapacity());
        assertEquals(Double.valueOf(10.5), event.getPrice());
        assertEquals("img", event.getImageURL());
        assertTrue(event.getGeolocationRequired());
        assertFalse(event.getIsEventActive());
        assertFalse(event.getJoinable());
    }

    @Test
    public void testToStringContainsKeyFields() {
        Event event = new Event();
        event.setEventID(99);
        event.setOrganizerID(11);
        event.setName("Concert");

        String out = event.toString();

        assertTrue(out.contains("99"));
        assertTrue(out.contains("11"));
        assertTrue(out.contains("Concert"));
    }
}