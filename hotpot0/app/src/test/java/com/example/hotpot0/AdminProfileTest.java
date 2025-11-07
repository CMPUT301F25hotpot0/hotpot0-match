package com.example.hotpot0;

import static org.junit.Assert.*;

import com.example.hotpot0.models.AdminProfile;

import org.junit.Test;

public class AdminProfileTest {

    @Test
    public void testDefaultConstructorAndSetters() {
        AdminProfile admin = new AdminProfile();

        admin.setAdminID(1);
        admin.setUsername("adminUser");
        admin.setPassword("password123");

        assertEquals(Integer.valueOf(1), admin.getAdminID());
        assertEquals("adminUser", admin.getUsername());
        assertEquals("password123", admin.getPassword());
    }

    @Test
    public void testParameterizedConstructor() {
        // Passing null for Context because it's not used
        AdminProfile admin = new AdminProfile(null, "superAdmin", "pass456");

        assertNull(admin.getAdminID()); // should be null initially
        assertEquals("superAdmin", admin.getUsername());
        assertEquals("pass456", admin.getPassword());
    }

    @Test
    public void testToStringMasksPassword() {
        AdminProfile admin = new AdminProfile();
        admin.setAdminID(10);
        admin.setUsername("admin1");
        admin.setPassword("secret");

        String str = admin.toString();
        assertTrue(str.contains("10"));
        assertTrue(str.contains("admin1"));
        assertTrue(str.contains("********")); // password should be masked
        assertFalse(str.contains("secret")); // raw password should not appear
    }
}