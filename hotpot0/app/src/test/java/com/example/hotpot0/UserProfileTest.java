package com.example.hotpot0;

import static org.junit.Assert.*;

import com.example.hotpot0.models.UserProfile;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class UserProfileTest {

    private UserProfile user;

    @Before
    public void setUp() {
        // Using test-friendly constructor
        user = new UserProfile("Alice", "alice@example.com", "1234567890", "FAKE_DEVICE_ID");
    }

    @Test
    public void testInitialValues() {
        assertNull(user.getUserID()); // Should be null initially
        assertEquals("FAKE_DEVICE_ID", user.getDeviceID());
        assertEquals("Alice", user.getName());
        assertEquals("alice@example.com", user.getEmailID());
        assertEquals("1234567890", user.getPhoneNumber());
        assertTrue(user.getNotificationsEnabled());
        assertEquals(37.7749, user.getLatitude(), 0.0001);
        assertEquals(-122.4194, user.getLongitude(), 0.0001);
        assertNotNull(user.getLinkIDs());
        assertTrue(user.getLinkIDs().isEmpty());
    }

    @Test
    public void testSettersAndGetters() {
        user.setUserID(101);
        assertEquals(Integer.valueOf(101), user.getUserID());

        user.setDeviceID("NEW_DEVICE_ID");
        assertEquals("NEW_DEVICE_ID", user.getDeviceID());

        user.setName("Bob");
        assertEquals("Bob", user.getName());

        user.setEmailID("bob@example.com");
        assertEquals("bob@example.com", user.getEmailID());

        user.setPhoneNumber("0987654321");
        assertEquals("0987654321", user.getPhoneNumber());

        user.setNotificationsEnabled(false);
        assertFalse(user.getNotificationsEnabled());

        user.setLatitude(12.3456);
        assertEquals(12.3456, user.getLatitude(), 0.0001);

        user.setLongitude(65.4321);
        assertEquals(65.4321, user.getLongitude(), 0.0001);

        ArrayList<String> links = new ArrayList<>();
        links.add("link1");
        links.add("link2");
        user.setLinkIDs(links);
        assertEquals(links, user.getLinkIDs());
    }

    @Test
    public void testAddAndRemoveLinkID() {
        user.addLinkID("event123");
        assertEquals(1, user.getLinkIDs().size());
        assertTrue(user.getLinkIDs().contains("event123"));

        user.addLinkID("event456");
        assertEquals(2, user.getLinkIDs().size());
        assertTrue(user.getLinkIDs().contains("event456"));

        user.removeLinkID("event123");
        assertEquals(1, user.getLinkIDs().size());
        assertFalse(user.getLinkIDs().contains("event123"));
    }

    @Test
    public void testToStringContainsAllFields() {
        String str = user.toString();
        assertTrue(str.contains("FAKE_DEVICE_ID"));
        assertTrue(str.contains("Alice"));
        assertTrue(str.contains("alice@example.com"));
        assertTrue(str.contains("1234567890"));
        assertTrue(str.contains("37.7749"));
        assertTrue(str.contains("-122.4194"));
    }
}