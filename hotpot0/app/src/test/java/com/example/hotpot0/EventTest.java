package com.example.hotpot0;

import static org.junit.Assert.*;

import com.example.hotpot0.models.Event;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventTest {

    @Test
    public void testDefaultConstructorAndSetters() {
        Event event = new Event();

        event.setEventID(1);
        event.setOrganizerID(100);
        event.setName("Tech Meetup");
        event.setDescription("A meetup for tech enthusiasts.");
        event.setGuidelines("Follow the rules.");
        event.setLocation("Toronto");
        event.setTime("18:00");
        event.setDate("2025-12-10");
        event.setDuration("2 hours");
        event.setRegistration_period("1 week");
        event.setCapacity(5);
        event.setPrice(20.0);
        event.setImageURL("http://example.com/image.jpg");
        event.setGeolocationRequired(true);
        event.setIsEventActive(false);

        ArrayList<String> links = new ArrayList<>();
        links.add("link1");
        event.setLinkIDs(links);

        ArrayList<String> sampled = new ArrayList<>();
        sampled.add("sample1");
        event.setSampledIDs(sampled);

        ArrayList<String> cancelled = new ArrayList<>();
        cancelled.add("cancel1");
        event.setCancelledIDs(cancelled);

        assertEquals(Integer.valueOf(1), event.getEventID());
        assertEquals(Integer.valueOf(100), event.getOrganizerID());
        assertEquals("Tech Meetup", event.getName());
        assertEquals("A meetup for tech enthusiasts.", event.getDescription());
        assertEquals("Follow the rules.", event.getGuidelines());
        assertEquals("Toronto", event.getLocation());
        assertEquals("18:00", event.getTime());
        assertEquals("2025-12-10", event.getDate());
        assertEquals("2 hours", event.getDuration());
        assertEquals("1 week", event.getRegistration_period());
        assertEquals(Integer.valueOf(5), event.getCapacity());
        assertEquals(Double.valueOf(20.0), event.getPrice());
        assertEquals("http://example.com/image.jpg", event.getImageURL());
        assertTrue(event.getGeolocationRequired());
        assertFalse(event.getIsEventActive());
        assertEquals(links, event.getLinkIDs());
        assertEquals(sampled, event.getSampledIDs());
        assertEquals(cancelled, event.getCancelledIDs());
    }

    @Test
    public void testAddAndRemoveLinkID() {
        Event event = new Event();
        event.setCapacity(2);

        assertTrue(event.addLinkID("link1"));
        assertTrue(event.addLinkID("link2"));
        assertFalse(event.addLinkID("link3")); // exceeds capacity
        assertFalse(event.addLinkID("link1")); // duplicate

        assertEquals(2, event.getLinkIDs().size());
        assertTrue(event.getLinkIDs().contains("link1"));

        assertTrue(event.removeLinkID("link1"));
        assertFalse(event.getLinkIDs().contains("link1"));
        assertFalse(event.removeLinkID("nonexistent"));
    }

    @Test
    public void testSampleParticipants() {
        Event event = new Event();
        event.setCapacity(3);

        List<String> waitlist = Arrays.asList("p1", "p2", "p3", "p4");
        ArrayList<String> sampled = event.sampleParticipants(waitlist);

        assertEquals(3, sampled.size()); // limited by capacity
        assertTrue(waitlist.containsAll(sampled));
        assertEquals(3, event.getSampledIDs().size());
    }

    @Test
    public void testFillSampledParticipants() {
        Event event = new Event();
        event.setCapacity(5);

        event.setSampledIDs(new ArrayList<>(Arrays.asList("p1", "p2")));
        List<String> waitlist = Arrays.asList("p3", "p4", "p5", "p6");

        ArrayList<String> newlySampled = event.fillSampledParticipants(waitlist);
        assertEquals(3, newlySampled.size()); // fills remaining spots
        assertTrue(event.getSampledIDs().containsAll(newlySampled));
        assertTrue(event.getSampledIDs().containsAll(Arrays.asList("p1", "p2")));
    }

    @Test
    public void testToString() {
        Event event = new Event();
        event.setEventID(10);
        event.setName("Music Festival");
        event.setOrganizerID(99);

        String str = event.toString();
        assertTrue(str.contains("10"));
        assertTrue(str.contains("Music Festival"));
        assertTrue(str.contains("99"));
    }
}
