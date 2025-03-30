package com.illumio.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LookupEntryTest {

    @Test
    void testEquals() {
        LookupEntry entry1 = new LookupEntry("80", "tcp", "web");
        LookupEntry entry2 = new LookupEntry("80", "tcp", "web");
        LookupEntry entry3 = new LookupEntry("80", "udp", "web");
        LookupEntry entry4 = new LookupEntry("443", "tcp", "web");

        // Same port and protocol should be equal
        assertEquals(entry1, entry2);
        
        // Different protocol should not be equal
        assertNotEquals(entry1, entry3);
        
        // Different port should not be equal
        assertNotEquals(entry1, entry4);
    }

    @Test
    void testHashCode() {
        LookupEntry entry1 = new LookupEntry("80", "tcp", "web");
        LookupEntry entry2 = new LookupEntry("80", "tcp", "web");
        LookupEntry entry3 = new LookupEntry("80", "udp", "web");

        // Same port and protocol should have same hash code
        assertEquals(entry1.hashCode(), entry2.hashCode());
        
        // Different protocol should have different hash code
        assertNotEquals(entry1.hashCode(), entry3.hashCode());
    }

    @Test
    void testCaseInsensitiveProtocol() {
        LookupEntry entry1 = new LookupEntry("80", "TCP", "web");
        LookupEntry entry2 = new LookupEntry("80", "tcp", "web");
        LookupEntry entry3 = new LookupEntry("80", "Tcp", "web");

        // All should be equal despite case differences
        assertEquals(entry1, entry2);
        assertEquals(entry1, entry3);
        assertEquals(entry2, entry3);
    }

    @Test
    void testGetters() {
        LookupEntry entry = new LookupEntry("80", "tcp", "web");
        
        assertEquals("80", entry.getDstPort());
        assertEquals("tcp", entry.getProtocol());
        assertEquals("web", entry.getTag());
    }
} 