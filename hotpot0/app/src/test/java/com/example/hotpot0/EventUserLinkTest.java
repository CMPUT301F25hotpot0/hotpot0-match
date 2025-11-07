package com.example.hotpot0;

import static org.junit.Assert.*;

import com.example.hotpot0.models.EventUserLink;

import org.junit.Test;

import java.util.List;

public class EventUserLinkTest {

    @Test
    public void testConstructorWithStatus() {
        EventUserLink link = new EventUserLink(1, 100, "Accepted");

        assertEquals(Integer.valueOf(1), link.getUserID());
        assertEquals(Integer.valueOf(100), link.getEventID());
        assertEquals("100_1", link.getLinkID());
        assertEquals("Accepted", link.getStatus());
        assertNotNull(link.getNotifications());
        assertTrue(link.getNotifications().isEmpty());
    }

    @Test
    public void testConstructorWithoutStatus() {
        EventUserLink link = new EventUserLink(2, 200);

        assertEquals(Integer.valueOf(2), link.getUserID());
        assertEquals(Integer.valueOf(200), link.getEventID());
        assertEquals("200_2", link.getLinkID());
        assertEquals("inWaitList", link.getStatus());
        assertNotNull(link.getNotifications());
        assertTrue(link.getNotifications().isEmpty());
    }

    @Test
    public void testAddNotification() {
        EventUserLink link = new EventUserLink(3, 300);

        link.addNotification("Notification 1");
        link.addNotification("Notification 2");

        List<String> notifications = link.getNotifications();
        assertEquals(2, notifications.size());
        assertTrue(notifications.contains("Notification 1"));
        assertTrue(notifications.contains("Notification 2"));
    }

    @Test
    public void testSetAndGetStatus_valid() {
        EventUserLink link = new EventUserLink(4, 400);
        link.setStatus("Sampled");

        assertEquals("Sampled", link.getStatus());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStatus_invalid_throwsException() {
        EventUserLink link = new EventUserLink(5, 500);
        link.setStatus("InvalidStatus"); // should throw exception
    }

    @Test
    public void testSetAndGetLinkID() {
        EventUserLink link = new EventUserLink(6, 600);
        link.setLinkID("customLinkID");

        assertEquals("customLinkID", link.getLinkID());
    }

    @Test
    public void testToString() {
        EventUserLink link = new EventUserLink(7, 700, "Declined");
        link.addNotification("Notification 1");

        String str = link.toString();
        assertTrue(str.contains("7"));
        assertTrue(str.contains("700"));
        assertTrue(str.contains("700_7"));
        assertTrue(str.contains("Declined"));
        assertTrue(str.contains("Notification 1"));
    }
}