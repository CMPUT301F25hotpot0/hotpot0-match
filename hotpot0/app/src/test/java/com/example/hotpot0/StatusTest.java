package com.example.hotpot0;

import static org.junit.Assert.*;

import com.example.hotpot0.models.Status;

import org.junit.Before;
import org.junit.Test;

public class StatusTest {

    private Status status;

    @Before
    public void setUp() {
        status = new Status();
    }

    @Test
    public void testSetValidStatuses() {
        String[] validStatuses = {"inWaitList", "Accepted", "Declined", "Organizer", "Sampled", "Cancelled"};
        for (String s : validStatuses) {
            status.setStatus(s);
            assertEquals(s, status.getStatus());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetInvalidStatusThrowsException() {
        status.setStatus("InvalidStatus"); // Should throw
    }

    @Test
    public void testDefaultStatusIsNull() {
        assertNull(status.getStatus());
    }
}