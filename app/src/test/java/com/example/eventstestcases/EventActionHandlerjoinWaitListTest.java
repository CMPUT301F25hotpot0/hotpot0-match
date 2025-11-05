package com.example.eventstestcases;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EventActionHandlerjoinWaitListTest {
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
    public void joinWaitListTest1() {
        // Assuming join is successful so return is 0 aka no error
        assertEquals(0, testHandler.joinWaitList(testUser.getUserID(), testEvent.getEventID()));
    }

    @Test
    public void joinWaitListTest2() {
        // User leaves event and tries to rejoin wait list
        testHandler.leaveWaitList(testUser.getUserID(), testEvent.getEventID());
        assertEquals(0, testHandler.joinWaitList(testUser.getUserID(), testEvent.getEventID()));
    }

    // Cleanup
    @After
    public void Cleanup() {
        testUser.deleteProfile();
        testOrganizer.deleteProfile();
        testEvent.deleteEvent();
    }
}