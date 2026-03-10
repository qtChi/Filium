package com.filium.simulation;

import com.filium.model.network.IPAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestARPTable {

    private ARPTable table;
    private final IPAddress ip1 = new IPAddress("192.168.1.1");
    private final IPAddress ip2 = new IPAddress("192.168.1.2");
    private static final String MAC1 = "AA:BB:CC:DD:EE:FF";
    private static final String MAC2 = "11:22:33:44:55:66";

    @BeforeEach void setUp() { table = new ARPTable(); }

    @Test void constructor_isEmpty() { assertEquals(0, table.size()); }

    // put
    @Test void put_nullIP_throws() {
        assertThrows(IllegalArgumentException.class, () -> table.put(null, MAC1));
    }
    @Test void put_nullMAC_throws() {
        assertThrows(IllegalArgumentException.class, () -> table.put(ip1, null));
    }
    @Test void put_blankMAC_throws() {
        assertThrows(IllegalArgumentException.class, () -> table.put(ip1, "  "));
    }
    @Test void put_valid_increaseSize() {
        table.put(ip1, MAC1);
        assertEquals(1, table.size());
    }
    @Test void put_sameIP_overwritesMAC() {
        table.put(ip1, MAC1); table.put(ip1, MAC2);
        assertEquals(MAC2, table.lookup(ip1).orElseThrow());
        assertEquals(1, table.size());
    }

    // lookup
    @Test void lookup_nullIP_throws() {
        assertThrows(IllegalArgumentException.class, () -> table.lookup(null));
    }
    @Test void lookup_knownIP_returnsMAC() {
        table.put(ip1, MAC1);
        assertEquals(MAC1, table.lookup(ip1).orElseThrow());
    }
    @Test void lookup_unknownIP_returnsEmpty() {
        assertTrue(table.lookup(ip2).isEmpty());
    }

    // contains
    @Test void contains_nullIP_throws() {
        assertThrows(IllegalArgumentException.class, () -> table.contains(null));
    }
    @Test void contains_knownIP_returnsTrue() {
        table.put(ip1, MAC1); assertTrue(table.contains(ip1));
    }
    @Test void contains_unknownIP_returnsFalse() {
        assertFalse(table.contains(ip2));
    }

    // remove
    @Test void remove_nullIP_throws() {
        assertThrows(IllegalArgumentException.class, () -> table.remove(null));
    }
    @Test void remove_knownIP_returnsTrue() {
        table.put(ip1, MAC1); assertTrue(table.remove(ip1));
    }
    @Test void remove_knownIP_entryGone() {
        table.put(ip1, MAC1); table.remove(ip1);
        assertFalse(table.contains(ip1));
    }
    @Test void remove_unknownIP_returnsFalse() {
        assertFalse(table.remove(ip2));
    }

    // getEntries / clear
    @Test void getEntries_returnsUnmodifiableMap() {
        assertThrows(UnsupportedOperationException.class,
            () -> table.getEntries().put("x", "y"));
    }
    @Test void clear_removesAllEntries() {
        table.put(ip1, MAC1); table.put(ip2, MAC2);
        table.clear();
        assertEquals(0, table.size());
    }
}