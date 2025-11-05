package com.example.eventstestcases;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EventActionHandlerleaveWaitListTest {
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
        testUser.joinWaitList(testEvent.getEventID());
    }
    // Run all tests sequentially
    @Test
    public void leaveWaitListTest1() {
        // Assuming leave is successful so return is 0 aka no error
        assertEquals(0, testHandler.leaveWaitList(testUser.getUserID(), testEvent.getEventID()));
    }

    @Test
    public void leaveWaitListTest2() {
        // User leaves event, rejoins the list and tries to leave waitlist again
        testHandler.leaveWaitList(testUser.getUserID(), testEvent.getEventID());
        testHandler.joinWaitList(testUser.getUserID(), testEvent.getEventID());
        assertEquals(0, testHandler.leaveWaitList(testUser.getUserID(), testEvent.getEventID()));
    }

    // Cleanup
    @After
    public void Cleanup() {
        testUser.deleteProfile();
        testOrganizer.deleteProfile();
        testEvent.deleteEvent();
    }
}
