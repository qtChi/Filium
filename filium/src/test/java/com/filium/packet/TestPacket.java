package com.filium.packet;

import com.filium.model.network.IPAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Packet.
 *
 * Input partitions covered:
 *  - Construction: valid with payload, valid with null payload (defaults to ""),
 *    null type, null header
 *  - getId(): non-null, UUID format, unique per instance
 *  - getType(): returns correct type
 *  - getHeader(): returns correct header
 *  - getPayload(): non-null string, empty string for null payload
 *  - getCreatedAt(): positive timestamp
 *  - withHeader(): returns new instance, preserves id and createdAt,
 *    updates header, null header throws
 *  - equals()/hashCode(): same id (via withHeader), different packets,
 *    same instance, null, different type
 *  - toString(): contains key fields
 */
class TestPacket {

    private PacketHeader header;
    private static final String SRC_MAC = "AA:BB:CC:DD:EE:FF";
    private static final String DST_MAC = "11:22:33:44:55:66";

    @BeforeEach
    void setUp() {
        header = new PacketHeader(
            new IPAddress("192.168.1.1"),
            new IPAddress("192.168.1.2"),
            SRC_MAC, DST_MAC, 64);
    }

    // ─────────────────────────────────────────────────────────────────
    // Construction — valid
    // ─────────────────────────────────────────────────────────────────

    @Test
    void constructor_validArgs_createsSuccessfully() {
        Packet p = new Packet(PacketType.ICMP_ECHO_REQUEST, header, "ping");
        assertNotNull(p);
    }

    @Test
    void constructor_nullPayload_defaultsToEmptyString() {
        Packet p = new Packet(PacketType.ICMP_ECHO_REQUEST, header, null);
        assertEquals("", p.getPayload());
    }

    @Test
    void constructor_emptyPayload_storesEmptyString() {
        Packet p = new Packet(PacketType.ICMP_ECHO_REQUEST, header, "");
        assertEquals("", p.getPayload());
    }

    // ─────────────────────────────────────────────────────────────────
    // Construction — invalid
    // ─────────────────────────────────────────────────────────────────

    @Test
    void constructor_nullType_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> new Packet(null, header, "payload"));
    }

    @Test
    void constructor_nullHeader_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> new Packet(PacketType.ICMP_ECHO_REQUEST, null, "payload"));
    }

    // ─────────────────────────────────────────────────────────────────
    // getId()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void getId_returnsNonNull() {
        assertNotNull(new Packet(PacketType.DNS_QUERY, header, "example.com").getId());
    }

    @Test
    void getId_isUUIDFormat() {
        String id = new Packet(PacketType.DNS_QUERY, header, "example.com").getId();
        // UUID format: 8-4-4-4-12 hex chars separated by hyphens
        assertTrue(id.matches(
            "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));
    }

    @Test
    void getId_uniquePerInstance() {
        Packet a = new Packet(PacketType.ICMP_ECHO_REQUEST, header, "");
        Packet b = new Packet(PacketType.ICMP_ECHO_REQUEST, header, "");
        assertNotEquals(a.getId(), b.getId());
    }

    // ─────────────────────────────────────────────────────────────────
    // Getters
    // ─────────────────────────────────────────────────────────────────

    @Test
    void getType_returnsCorrectType() {
        Packet p = new Packet(PacketType.ARP_REQUEST, header, "");
        assertEquals(PacketType.ARP_REQUEST, p.getType());
    }

    @Test
    void getHeader_returnsCorrectHeader() {
        Packet p = new Packet(PacketType.DNS_QUERY, header, "host.local");
        assertEquals(header, p.getHeader());
    }

    @Test
    void getPayload_returnsCorrectPayload() {
        Packet p = new Packet(PacketType.HTTP_REQUEST, header, "GET / HTTP/1.1");
        assertEquals("GET / HTTP/1.1", p.getPayload());
    }

    @Test
    void getCreatedAt_returnsPositiveTimestamp() {
        Packet p = new Packet(PacketType.ICMP_ECHO_REQUEST, header, "");
        assertTrue(p.getCreatedAt() > 0);
    }

    // ─────────────────────────────────────────────────────────────────
    // withHeader()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void withHeader_returnsNewInstance() {
        Packet original = new Packet(PacketType.ICMP_ECHO_REQUEST, header, "");
        PacketHeader newHeader = new PacketHeader(
            new IPAddress("10.0.0.1"),
            new IPAddress("10.0.0.2"),
            SRC_MAC, DST_MAC, 63);
        Packet updated = original.withHeader(newHeader);
        assertNotSame(original, updated);
    }

    @Test
    void withHeader_preservesId() {
        Packet original = new Packet(PacketType.ICMP_ECHO_REQUEST, header, "");
        PacketHeader newHeader = new PacketHeader(
            new IPAddress("10.0.0.1"),
            new IPAddress("10.0.0.2"),
            SRC_MAC, DST_MAC, 63);
        Packet updated = original.withHeader(newHeader);
        assertEquals(original.getId(), updated.getId());
    }

    @Test
    void withHeader_preservesCreatedAt() {
        Packet original = new Packet(PacketType.ICMP_ECHO_REQUEST, header, "");
        PacketHeader newHeader = new PacketHeader(
            new IPAddress("10.0.0.1"),
            new IPAddress("10.0.0.2"),
            SRC_MAC, DST_MAC, 63);
        Packet updated = original.withHeader(newHeader);
        assertEquals(original.getCreatedAt(), updated.getCreatedAt());
    }

    @Test
    void withHeader_updatesHeader() {
        Packet original = new Packet(PacketType.ICMP_ECHO_REQUEST, header, "");
        PacketHeader newHeader = new PacketHeader(
            new IPAddress("10.0.0.1"),
            new IPAddress("10.0.0.2"),
            SRC_MAC, DST_MAC, 63);
        Packet updated = original.withHeader(newHeader);
        assertEquals(newHeader, updated.getHeader());
    }

    @Test
    void withHeader_originalHeaderUnchanged() {
        Packet original = new Packet(PacketType.ICMP_ECHO_REQUEST, header, "");
        PacketHeader newHeader = new PacketHeader(
            new IPAddress("10.0.0.1"),
            new IPAddress("10.0.0.2"),
            SRC_MAC, DST_MAC, 63);
        original.withHeader(newHeader);
        assertEquals(header, original.getHeader());
    }

    @Test
    void withHeader_nullHeader_throwsIllegalArgumentException() {
        Packet original = new Packet(PacketType.ICMP_ECHO_REQUEST, header, "");
        assertThrows(IllegalArgumentException.class,
            () -> original.withHeader(null));
    }

    // ─────────────────────────────────────────────────────────────────
    // equals() and hashCode()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void equals_sameInstance_returnsTrue() {
        Packet p = new Packet(PacketType.ICMP_ECHO_REQUEST, header, "");
        assertEquals(p, p);
    }

    @Test
    void equals_sameIdViaWithHeader_returnsTrue() {
        Packet original = new Packet(PacketType.ICMP_ECHO_REQUEST, header, "ping");
        PacketHeader newHeader = new PacketHeader(
            new IPAddress("10.0.0.1"),
            new IPAddress("10.0.0.2"),
            SRC_MAC, DST_MAC, 63);
        Packet updated = original.withHeader(newHeader);
        assertEquals(original, updated);
    }

    @Test
    void equals_differentPackets_returnsFalse() {
        Packet a = new Packet(PacketType.ICMP_ECHO_REQUEST, header, "");
        Packet b = new Packet(PacketType.ICMP_ECHO_REQUEST, header, "");
        assertNotEquals(a, b);
    }

    @Test
    void equals_null_returnsFalse() {
        Packet p = new Packet(PacketType.ICMP_ECHO_REQUEST, header, "");
        assertNotEquals(null, p);
    }

    @Test
    void equals_differentType_returnsFalse() {
        Packet p = new Packet(PacketType.ICMP_ECHO_REQUEST, header, "");
        assertNotEquals("not a packet", p);
    }

    @Test
    void hashCode_sameIdPackets_sameHashCode() {
        Packet original = new Packet(PacketType.ICMP_ECHO_REQUEST, header, "");
        PacketHeader newHeader = new PacketHeader(
            new IPAddress("10.0.0.1"),
            new IPAddress("10.0.0.2"),
            SRC_MAC, DST_MAC, 63);
        Packet updated = original.withHeader(newHeader);
        assertEquals(original.hashCode(), updated.hashCode());
    }

    @Test
    void hashCode_differentPackets_differentHashCode() {
        Packet a = new Packet(PacketType.ICMP_ECHO_REQUEST, header, "");
        Packet b = new Packet(PacketType.ICMP_ECHO_REQUEST, header, "");
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    // ─────────────────────────────────────────────────────────────────
    // toString()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void toString_containsPacketType() {
        Packet p = new Packet(PacketType.ICMP_ECHO_REQUEST, header, "");
        assertTrue(p.toString().contains("ICMP_ECHO_REQUEST"));
    }

    @Test
    void toString_containsSourceIP() {
        Packet p = new Packet(PacketType.ICMP_ECHO_REQUEST, header, "");
        assertTrue(p.toString().contains("192.168.1.1"));
    }

    @Test
    void toString_containsDestinationIP() {
        Packet p = new Packet(PacketType.ICMP_ECHO_REQUEST, header, "");
        assertTrue(p.toString().contains("192.168.1.2"));
    }
}