package com.filium.model.devices;

import com.filium.model.network.IPAddress;
import com.filium.packet.Packet;
import com.filium.packet.PacketHeader;
import com.filium.packet.PacketType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DNSServer.
 *
 * Input partitions covered:
 *  - Construction: correct DeviceType, records empty initially
 *  - addRecord(): valid hostname+ip, null hostname, blank hostname, null ip
 *  - removeRecord(): known hostname removed returns true,
 *    unknown hostname returns false
 *  - lookup(): found returns non-empty Optional,
 *    not found returns empty Optional
 *  - getRecords(): unmodifiable
 *  - receivePacket(): valid packet accepted, null throws
 *  - reset(): clears records
 */
class TestDNSServer {

    private static final String SRC_MAC = "AA:BB:CC:DD:EE:FF";
    private static final String DST_MAC = "11:22:33:44:55:66";

    private DNSServer dns;

    @BeforeEach
    void setUp() {
        dns = new DNSServer("DNS-1");
    }

    private Packet samplePacket() {
        PacketHeader h = new PacketHeader(
            new IPAddress("192.168.1.1"), new IPAddress("192.168.1.10"),
            SRC_MAC, DST_MAC, 64);
        return new Packet(PacketType.DNS_QUERY, h, "example.com");
    }

    // ─────────────────────────────────────────────────────────────────
    // Construction
    // ─────────────────────────────────────────────────────────────────

    @Test
    void constructor_setsCorrectDeviceType() {
        assertEquals(DeviceType.DNS_SERVER, dns.getDeviceType());
    }

    @Test
    void constructor_recordsEmptyInitially() {
        assertTrue(dns.getRecords().isEmpty());
    }

    // ─────────────────────────────────────────────────────────────────
    // addRecord()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void addRecord_validArgs_storesRecord() {
        IPAddress ip = new IPAddress("93.184.216.34");
        dns.addRecord("example.com", ip);
        assertEquals(Optional.of(ip), dns.lookup("example.com"));
    }

    @Test
    void addRecord_overwritesExistingRecord() {
        dns.addRecord("example.com", new IPAddress("1.1.1.1"));
        IPAddress updated = new IPAddress("2.2.2.2");
        dns.addRecord("example.com", updated);
        assertEquals(Optional.of(updated), dns.lookup("example.com"));
    }

    @Test
    void addRecord_nullHostname_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> dns.addRecord(null, new IPAddress("1.1.1.1")));
    }

    @Test
    void addRecord_blankHostname_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> dns.addRecord("  ", new IPAddress("1.1.1.1")));
    }

    @Test
    void addRecord_nullIP_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> dns.addRecord("example.com", null));
    }

    // ─────────────────────────────────────────────────────────────────
    // removeRecord()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void removeRecord_existingHostname_returnsTrue() {
        dns.addRecord("example.com", new IPAddress("1.1.1.1"));
        assertTrue(dns.removeRecord("example.com"));
    }

    @Test
    void removeRecord_existingHostname_recordIsGone() {
        dns.addRecord("example.com", new IPAddress("1.1.1.1"));
        dns.removeRecord("example.com");
        assertTrue(dns.lookup("example.com").isEmpty());
    }

    @Test
    void removeRecord_unknownHostname_returnsFalse() {
        assertFalse(dns.removeRecord("notfound.com"));
    }

    // ─────────────────────────────────────────────────────────────────
    // lookup()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void lookup_knownHostname_returnsNonEmptyOptional() {
        dns.addRecord("host.local", new IPAddress("10.0.0.5"));
        assertTrue(dns.lookup("host.local").isPresent());
    }

    @Test
    void lookup_unknownHostname_returnsEmptyOptional() {
        assertTrue(dns.lookup("unknown.local").isEmpty());
    }

    // ─────────────────────────────────────────────────────────────────
    // getRecords()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void getRecords_returnsUnmodifiableMap() {
        assertThrows(UnsupportedOperationException.class,
            () -> dns.getRecords().put("x.com", new IPAddress("1.1.1.1")));
    }

    // ─────────────────────────────────────────────────────────────────
    // receivePacket()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void receivePacket_validPacket_doesNotThrow() {
        assertDoesNotThrow(() -> dns.receivePacket(samplePacket()));
    }

    @Test
    void receivePacket_null_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> dns.receivePacket(null));
    }

    // ─────────────────────────────────────────────────────────────────
    // reset()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void reset_clearsRecords() {
        dns.addRecord("example.com", new IPAddress("1.1.1.1"));
        dns.reset();
        assertTrue(dns.getRecords().isEmpty());
    }

    @Test
    void reset_onEmptyRecords_doesNotThrow() {
        assertDoesNotThrow(() -> dns.reset());
    }
}