package com.example.hotpot0;

import static org.junit.Assert.*;

import com.example.hotpot0.models.EventUserLink;
import com.example.hotpot0.models.Notification;
import com.example.hotpot0.models.Status;

import org.junit.Test;

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
    public void testConstructorWithLatLong() {
        EventUserLink link = new EventUserLink(2, 200, 53.5, -113.4);

        assertEquals(Integer.valueOf(2), link.getUserID());
        assertEquals(Integer.valueOf(200), link.getEventID());
        assertEquals("200_2", link.getLinkID());
        assertEquals("inWaitList", link.getStatus());

        assertEquals(Double.valueOf(53.5), link.getLatitude());
        assertEquals(Double.valueOf(-113.4), link.getLongitude());
    }

    @Test
    public void testSetAndGetStatus_valid() {
        EventUserLink link = new EventUserLink(3, 300, "Accepted");
        link.setStatus("Cancelled");

        assertEquals("Cancelled", link.getStatus());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetStatus_invalid_throwsException() {
        EventUserLink link = new EventUserLink(4, 400, "Accepted");
        link.setStatus("NotAStatus"); // Should throw
    }

    @Test
    public void testSetAndGetLatitudeLongitude() {
        EventUserLink link = new EventUserLink(5, 500, 10.0, 20.0);

        link.setLatitude(11.1);
        link.setLongitude(22.2);

        assertEquals(Double.valueOf(11.1), link.getLatitude());
        assertEquals(Double.valueOf(22.2), link.getLongitude());
    }

    @Test
    public void testAddNotification_AutomaticNotification() {
        EventUserLink link = new EventUserLink(6, 600, "Accepted");

        Status status = new Status();
        status.setStatus("Sampled");

        Notification notif = new Notification(
                "2025-03-10",
                status,
                "My Event",
                600
        );

        link.addNotification(notif);

        assertEquals(1, link.getNotifications().size());
        assertEquals(notif, link.getNotifications().get(0));
    }

    @Test
    public void testAddNotification_CustomNotification() {
        EventUserLink link = new EventUserLink(7, 700, "Accepted");

        Status status = new Status();
        status.setStatus("Declined");

        Notification notif = new Notification(
                "2025-03-12",
                status,
                "Custom message",
                "Mega Event",
                700,
                true
        );

        link.addNotification(notif);

        assertEquals(1, link.getNotifications().size());
        assertEquals("Custom message", link.getNotifications().get(0).getText());
        assertTrue(link.getNotifications().get(0).isCustomNotif());
    }

    @Test
    public void testAddNotification_ResampledNotification() {
        EventUserLink link = new EventUserLink(8, 800, "inWaitList");

        Status status = new Status();
        status.setStatus("Sampled");

        Notification notif = new Notification(
                "2025-03-12",
                status,
                true,
                "Event XYZ",
                800
        );

        link.addNotification(notif);

        assertEquals(1, link.getNotifications().size());
        assertTrue(link.getNotifications().get(0).isResampledNotif());
    }

    @Test
    public void testSetAndGetLinkID() {
        EventUserLink link = new EventUserLink(9, 900, "Accepted");

        link.setLinkID("customLinkID");

        assertEquals("customLinkID", link.getLinkID());
    }

    @Test
    public void testToString_containsImportantFields() {
        EventUserLink link = new EventUserLink(10, 1000, "Declined");

        Status status = new Status();
        status.setStatus("Sampled");

        Notification notif = new Notification(
                "2025-03-14",
                status,
                "Event ABC",
                1000
        );

        link.addNotification(notif);

        String result = link.toString();

        assertTrue(result.contains("10"));
        assertTrue(result.contains("1000"));
        assertTrue(result.contains("1000_10"));
        assertTrue(result.contains("Declined"));
    }
}