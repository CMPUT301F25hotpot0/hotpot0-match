package com.example.hotpot0;

import static org.junit.Assert.*;

import com.example.hotpot0.models.Notification;
import com.example.hotpot0.models.Status;

import org.junit.Before;
import org.junit.Test;

public class NotificationTest {

    private Status statusSampled;
    private Status statusWaitlist;

    @Before
    public void setUp() {
        statusSampled = new Status();
        statusSampled.setStatus("Sampled");

        statusWaitlist = new Status();
        statusWaitlist.setStatus("inWaitList");
    }

    @Test
    public void testAutomaticNotificationText() {
        Notification notif = new Notification("2025-12-01 10:00", statusSampled, "Hackathon", 1);
        assertEquals("Congratulations! You have been chosen to attend the event 'Hackathon'. Please confirm your attendance for the event by accepting/declining your invitation.",
                notif.getText());
        assertFalse(notif.isCustomNotif());
        assertFalse(notif.isResampledNotif());
    }

    @Test
    public void testAutomaticNotificationTextWithWaitlist() {
        Notification notif = new Notification("2025-12-01 10:00", statusWaitlist, "Hackathon", 2);
        assertEquals("Sorry! You have lost the lottery of attending the event 'Hackathon'. Fret not - you are still part of the waiting list and have a chance of being chosen again if slots become available. If you wish to leave the waitlist, you can do so from the event page.",
                notif.getText());
    }

    @Test
    public void testCustomNotification() {
        String customText = "Custom message for your event.";
        Notification notif = new Notification("2025-12-01 10:00", statusSampled, customText, "Hackathon", 3, true);
        assertEquals(customText, notif.getText());
        assertTrue(notif.isCustomNotif());
        assertFalse(notif.isResampledNotif());
    }

    @Test
    public void testResampledNotification() {
        Notification notif = new Notification("2025-12-01 10:00", statusSampled, true, "Hackathon", 4);
        assertEquals("Congratulations! You have been chosen to attend the event 'Hackathon'. Please confirm your attendance for the event by accepting/declining your invitation.",
                notif.getText());
        assertFalse(notif.isCustomNotif());
        assertTrue(notif.isResampledNotif());
    }

    @Test
    public void testEventNameDefault() {
        Notification notif = new Notification("2025-12-01 10:00", statusSampled, null, 5);
        assertEquals("Congratulations! You have been chosen to attend the event 'Event'. Please confirm your attendance for the event by accepting/declining your invitation.",
                notif.getText());
    }
}
