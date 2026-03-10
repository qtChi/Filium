package com.filium.simulation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SimulationEventType enum.
 *
 * Input partitions covered:
 *  - All 10 enum values exist and are accessible
 *  - values() count matches expected
 *  - valueOf() round-trips correctly for each constant
 *  - Logical groupings are correct (packet lifecycle, protocol, firewall)
 */
class TestSimulationEventType {

    @Test
    void values_containsAllTenEventTypes() {
        assertEquals(10, SimulationEventType.values().length);
    }

    // ─────────────────────────────────────────────────────────────────
    // Packet lifecycle values
    // ─────────────────────────────────────────────────────────────────

    @Test
    void packetSent_exists() {
        assertNotNull(SimulationEventType.PACKET_SENT);
    }

    @Test
    void packetReceived_exists() {
        assertNotNull(SimulationEventType.PACKET_RECEIVED);
    }

    @Test
    void packetDropped_exists() {
        assertNotNull(SimulationEventType.PACKET_DROPPED);
    }

    @Test
    void packetTtlExpired_exists() {
        assertNotNull(SimulationEventType.PACKET_TTL_EXPIRED);
    }

    // ─────────────────────────────────────────────────────────────────
    // Protocol resolution values
    // ─────────────────────────────────────────────────────────────────

    @Test
    void dnsResolved_exists() {
        assertNotNull(SimulationEventType.DNS_RESOLVED);
    }

    @Test
    void dnsFailed_exists() {
        assertNotNull(SimulationEventType.DNS_FAILED);
    }

    @Test
    void dhcpAssigned_exists() {
        assertNotNull(SimulationEventType.DHCP_ASSIGNED);
    }

    @Test
    void arpResolved_exists() {
        assertNotNull(SimulationEventType.ARP_RESOLVED);
    }

    // ─────────────────────────────────────────────────────────────────
    // Firewall values
    // ─────────────────────────────────────────────────────────────────

    @Test
    void firewallBlocked_exists() {
        assertNotNull(SimulationEventType.FIREWALL_BLOCKED);
    }

    @Test
    void firewallAllowed_exists() {
        assertNotNull(SimulationEventType.FIREWALL_ALLOWED);
    }

    // ─────────────────────────────────────────────────────────────────
    // valueOf() round-trip
    // ─────────────────────────────────────────────────────────────────

    @Test
    void valueOf_allConstants_roundTripSuccessfully() {
        for (SimulationEventType type : SimulationEventType.values()) {
            assertEquals(type, SimulationEventType.valueOf(type.name()));
        }
    }

    @Test
    void valueOf_unknownName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> SimulationEventType.valueOf("UNKNOWN_EVENT"));
    }
}