package com.filium.packet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PacketType enum.
 *
 * Input partitions covered:
 *  - All 13 enum values exist and are accessible
 *  - values() count matches expected
 *  - valueOf() round-trips correctly for each constant
 *  - Logical groupings are correct (ICMP, ARP, DNS, DHCP, HTTP)
 */
class TestPacketType {

    @Test
    void values_containsAllThirteenPacketTypes() {
        assertEquals(13, PacketType.values().length);
    }

    // ─────────────────────────────────────────────────────────────────
    // ICMP values
    // ─────────────────────────────────────────────────────────────────

    @Test
    void icmpEchoRequest_exists() {
        assertNotNull(PacketType.ICMP_ECHO_REQUEST);
    }

    @Test
    void icmpEchoReply_exists() {
        assertNotNull(PacketType.ICMP_ECHO_REPLY);
    }

    @Test
    void icmpUnreachable_exists() {
        assertNotNull(PacketType.ICMP_UNREACHABLE);
    }

    // ─────────────────────────────────────────────────────────────────
    // ARP values
    // ─────────────────────────────────────────────────────────────────

    @Test
    void arpRequest_exists() {
        assertNotNull(PacketType.ARP_REQUEST);
    }

    @Test
    void arpReply_exists() {
        assertNotNull(PacketType.ARP_REPLY);
    }

    // ─────────────────────────────────────────────────────────────────
    // DNS values
    // ─────────────────────────────────────────────────────────────────

    @Test
    void dnsQuery_exists() {
        assertNotNull(PacketType.DNS_QUERY);
    }

    @Test
    void dnsResponse_exists() {
        assertNotNull(PacketType.DNS_RESPONSE);
    }

    // ─────────────────────────────────────────────────────────────────
    // DHCP values
    // ─────────────────────────────────────────────────────────────────

    @Test
    void dhcpDiscover_exists() {
        assertNotNull(PacketType.DHCP_DISCOVER);
    }

    @Test
    void dhcpOffer_exists() {
        assertNotNull(PacketType.DHCP_OFFER);
    }

    @Test
    void dhcpRequest_exists() {
        assertNotNull(PacketType.DHCP_REQUEST);
    }

    @Test
    void dhcpAck_exists() {
        assertNotNull(PacketType.DHCP_ACK);
    }

    // ─────────────────────────────────────────────────────────────────
    // HTTP values
    // ─────────────────────────────────────────────────────────────────

    @Test
    void httpRequest_exists() {
        assertNotNull(PacketType.HTTP_REQUEST);
    }

    @Test
    void httpResponse_exists() {
        assertNotNull(PacketType.HTTP_RESPONSE);
    }

    // ─────────────────────────────────────────────────────────────────
    // valueOf() round-trip
    // ─────────────────────────────────────────────────────────────────

    @Test
    void valueOf_allConstants_roundTripSuccessfully() {
        for (PacketType type : PacketType.values()) {
            assertEquals(type, PacketType.valueOf(type.name()));
        }
    }

    @Test
    void valueOf_unknownName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> PacketType.valueOf("UNKNOWN_PACKET"));
    }
}