package com.filium.simulation;

import com.filium.model.network.IPAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestRoutingTable {

    private RoutingTable table;

    @BeforeEach void setUp() { table = new RoutingTable(); }

    private RoutingTable.RouteEntry entry(String net, String cidr, String hop) {
        return new RoutingTable.RouteEntry(net, cidr, new IPAddress(hop));
    }

    // RouteEntry construction validation
    @Test void routeEntry_nullNetwork_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> new RoutingTable.RouteEntry(null, "24", new IPAddress("10.0.0.1")));
    }
    @Test void routeEntry_blankNetwork_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> new RoutingTable.RouteEntry("  ", "24", new IPAddress("10.0.0.1")));
    }
    @Test void routeEntry_nullCidr_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> new RoutingTable.RouteEntry("10.0.0.0", null, new IPAddress("10.0.0.1")));
    }
    @Test void routeEntry_blankCidr_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> new RoutingTable.RouteEntry("10.0.0.0", "  ", new IPAddress("10.0.0.1")));
    }
    @Test void routeEntry_nullNextHop_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> new RoutingTable.RouteEntry("10.0.0.0", "24", null));
    }

    // RouteEntry getters
    @Test void routeEntry_getters_returnCorrectValues() {
        RoutingTable.RouteEntry e = entry("10.0.0.0", "24", "10.0.0.1");
        assertEquals("10.0.0.0", e.getNetwork());
        assertEquals("24", e.getCidrMask());
        assertEquals(new IPAddress("10.0.0.1"), e.getNextHop());
    }

    @Test void routeEntry_toString_containsNetworkAndHop() {
        String s = entry("10.0.0.0", "24", "10.0.0.1").toString();
        assertTrue(s.contains("10.0.0.0"));
        assertTrue(s.contains("10.0.0.1"));
    }

    // addRoute / size
    @Test void addRoute_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> table.addRoute(null));
    }
    @Test void addRoute_validEntry_increaseSize() {
        table.addRoute(entry("192.168.1.0", "24", "192.168.1.1"));
        assertEquals(1, table.size());
    }
    @Test void addRoute_duplicateKey_replacesEntry() {
        table.addRoute(entry("192.168.1.0", "24", "10.0.0.1"));
        table.addRoute(entry("192.168.1.0", "24", "10.0.0.2"));
        assertEquals(1, table.size());
    }

    // removeRoute
    @Test void removeRoute_existingKey_returnsTrue() {
        table.addRoute(entry("192.168.1.0", "24", "10.0.0.1"));
        assertTrue(table.removeRoute("192.168.1.0", "24"));
    }
    @Test void removeRoute_unknownKey_returnsFalse() {
        assertFalse(table.removeRoute("10.0.0.0", "8"));
    }

    // lookup
    @Test void lookup_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> table.lookup(null));
    }
    @Test void lookup_matchingRoute_returnsNextHop() {
        table.addRoute(entry("192.168.1.0", "24", "192.168.1.254"));
        assertTrue(table.lookup(new IPAddress("192.168.1.50")).isPresent());
        assertEquals(new IPAddress("192.168.1.254"),
            table.lookup(new IPAddress("192.168.1.50")).get());
    }
    @Test void lookup_noMatchingRoute_returnsEmpty() {
        table.addRoute(entry("192.168.1.0", "24", "192.168.1.254"));
        assertTrue(table.lookup(new IPAddress("10.0.0.5")).isEmpty());
    }
    @Test void lookup_firstMatchWins() {
        table.addRoute(entry("10.0.0.0", "8", "10.255.255.1"));
        table.addRoute(entry("10.0.0.0", "24", "10.0.0.254"));
        // Both match 10.0.0.5 — first inserted wins
        assertEquals(new IPAddress("10.255.255.1"),
            table.lookup(new IPAddress("10.0.0.5")).get());
    }

    // getEntries / clear
    @Test void getEntries_returnsUnmodifiableMap() {
        assertThrows(UnsupportedOperationException.class,
            () -> table.getEntries().put("x", entry("10.0.0.0","8","10.0.0.1")));
    }
    @Test void clear_removesAllEntries() {
        table.addRoute(entry("192.168.1.0", "24", "192.168.1.1"));
        table.clear();
        assertEquals(0, table.size());
    }
}