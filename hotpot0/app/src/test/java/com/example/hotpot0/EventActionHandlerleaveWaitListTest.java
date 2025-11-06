package com.example.hotpot0;

import com.example.hotpot0.models.UserProfile;
import com.example.hotpot0.models.Event;
import com.example.hotpot0.section2.controllers.EventActionHandler;
import com.google.firebase.firestore.auth.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class EventActionHandlerleaveWaitListTest {
    // Test Models
    private UserProfile testUser;
    private UserProfile testOrganizer;
    private Event testEvent;
    private EventActionHandler testHandler;

    @Before
    public void setUp() {
        testUser = new User("Test User", "test@testcase.com", "1234567890");
        testOrganizer = new User("Test Organizer", "organizer@testcase.com", "1029384756");
        testEvent = new Event(testOrganizer.getUserID(), "Test Event", "This is a test event.",
                "No guidelines", "Test Location", "12:00 PM", "2024-12-31",
                "2 days", 5, 0.0, "5 days",
                "image.url", false);
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
