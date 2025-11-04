package com.example.eventstestcases;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class EventActionHandlerjoinEventTest {
    // Test Models
    private User testUser;
    private User testOrganizer;
    private Event testEvent;
    private EventActionHandler testHandler;

    @Before
    public void setUp() {
        testUser = new User("Test User", "test@testcase.com", "1234567890");
        testOrganizer = new User("Test Organizer", "organizer@testcase.com", "1029384756");
        testEvent = new Event(testOrganizer.getUserID(), "Test Event", "This is a test event", "LocationX", "12:00", "01-01-26", null, 500, 10, "10-10-25", "12-12-25");
        testHandler = new EventActionHandler();
    }
    // Run all tests sequentially
    @Test
    public void testjoinEvent1() {
        // Assuming join is successful so return is 0 aka no error
        assertEquals(0, testUser.joinEvent(testEvent.getEventID()));
    }

    @Test
    public void testjoinEvent2() {
        // Assuming user has already joined waiting list for the event from test1,
        // they cannot join the waiting list again, hence error code 1
        assertEquals(1, testUser.joinEvent(testEvent.getEventID()));
    }

    @Test
    public void testjoinEvent3() {
        // User leaves event and tries to rejoin waiting list
        testHandler.leaveWaitingList(testUser.getUserID(), testEvent.getEventID());
        assertEquals(0, testUser.joinEvent(testEvent.getEventID()));
    }

    @Test
    public void testjoinEvent4() {
        // Set status to accepted and try to join
        // User should not be able to have an option to join event, but testing regardless
        testHandler.sampleUser(testUser.getUserID(), testEvent.getEventID());
        testHandler.acceptEvent(testUser.getUserID(), testEvent.getEventID());
        assertEquals(2,testUser.joinEvent(testEvent.getEventID()));
    }

    @Test
    public void testjoinEvent5() {
        // Set status to declined and try to join
        // User should not be able to have an option to join event, but testing regardless
        // Assumption is that if user declines, they don't want to be part of the event any longer
        testHandler.sampleUser(testUser.getUserID(), testEvent.getEventID());
        testHandler.declineEvent(testUser.getUserID(), testEvent.getEventID());
        assertEquals(3,testUser.joinEvent(testEvent.getEventID()));
    }

    @Test
    public void testjoinEvent6() {
        // Organizer trying to join the waiting list
        assertEquals(99,testOrganizer.joinEvent(testEvent.getEventID()));
    }

    @Test
    public void testjoinEvent7() {
        // Set status to sampled and try to join
        // User should not be able to have an option to join event, but testing regardless
        testHandler.sampleUser(testUser.getUserID(), testEvent.getEventID());
        assertEquals(4, testUser.joinEvent(testEvent.getEventID()));
    }

    @Test
    public void testjoinEvent8() {
        // Set status to cancelled and try to join
        // User should not be able to have an option to join event, but testing regardless
        testHandler.cancelUser(testUser.getUserID(), testEvent.getEventID());
        assertEquals(-1, testUser.joinEvent(testEvent.getEventID()));
    }

    // Cleanup
    @After
    public void Cleanup() {
        testUser.deleteProfile();
        testOrganizer.deleteProfile();
        testEvent.deleteEvent();
    }
}