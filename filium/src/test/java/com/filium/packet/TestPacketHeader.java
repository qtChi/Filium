package com.filium.packet;

import com.filium.model.network.IPAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PacketHeader.
 *
 * Input partitions covered:
 *  - Construction: valid typical, null sourceIP, null destinationIP,
 *    null sourceMAC, invalid sourceMAC, null destinationMAC,
 *    invalid destinationMAC, TTL=1 (min), TTL=255 (max),
 *    TTL=0 (below min), TTL=256 (above max)
 *  - getters: all fields returned correctly
 *  - decrementTTL(): TTL > 1 returns new header with ttl-1,
 *    TTL=1 returns expired header, expired header throws
 *  - isExpired(): false for TTL > 0, true after decrement to 0
 *  - equals()/hashCode(): same fields, one field different, same instance,
 *    null, different type
 *  - toString(): contains key fields
 */
class TestPacketHeader {

    private IPAddress srcIP;
    private IPAddress dstIP;
    private static final String SRC_MAC = "AA:BB:CC:DD:EE:FF";
    private static final String DST_MAC = "11:22:33:44:55:66";

    @BeforeEach
    void setUp() {
        srcIP = new IPAddress("192.168.1.1");
        dstIP = new IPAddress("192.168.1.2");
    }

    private PacketHeader header(int ttl) {
        return new PacketHeader(srcIP, dstIP, SRC_MAC, DST_MAC, ttl);
    }

    // ─────────────────────────────────────────────────────────────────
    // Construction — valid
    // ─────────────────────────────────────────────────────────────────

    @Test
    void constructor_validArgs_createsSuccessfully() {
        PacketHeader h = header(64);
        assertNotNull(h);
    }

    @Test
    void constructor_ttlMin_createsSuccessfully() {
        assertDoesNotThrow(() -> header(1));
    }

    @Test
    void constructor_ttlMax_createsSuccessfully() {
        assertDoesNotThrow(() -> header(255));
    }

    // ─────────────────────────────────────────────────────────────────
    // Construction — invalid
    // ─────────────────────────────────────────────────────────────────

    @Test
    void constructor_nullSourceIP_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> new PacketHeader(null, dstIP, SRC_MAC, DST_MAC, 64));
    }

    @Test
    void constructor_nullDestinationIP_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> new PacketHeader(srcIP, null, SRC_MAC, DST_MAC, 64));
    }

    @Test
    void constructor_nullSourceMAC_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> new PacketHeader(srcIP, dstIP, null, DST_MAC, 64));
    }

    @Test
    void constructor_invalidSourceMAC_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> new PacketHeader(srcIP, dstIP, "not-a-mac", DST_MAC, 64));
    }

    @Test
    void constructor_nullDestinationMAC_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> new PacketHeader(srcIP, dstIP, SRC_MAC, null, 64));
    }

    @Test
    void constructor_invalidDestinationMAC_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> new PacketHeader(srcIP, dstIP, SRC_MAC, "not-a-mac", 64));
    }

    @Test
    void constructor_ttlZero_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> header(0));
    }

    @Test
    void constructor_ttlNegative_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> header(-1));
    }

    @Test
    void constructor_ttlAbove255_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> header(256));
    }

    // ─────────────────────────────────────────────────────────────────
    // Getters
    // ─────────────────────────────────────────────────────────────────

    @Test
    void getSourceIP_returnsCorrectIP() {
        assertEquals(srcIP, header(64).getSourceIP());
    }

    @Test
    void getDestinationIP_returnsCorrectIP() {
        assertEquals(dstIP, header(64).getDestinationIP());
    }

    @Test
    void getSourceMAC_returnsCorrectMAC() {
        assertEquals(SRC_MAC, header(64).getSourceMAC());
    }

    @Test
    void getDestinationMAC_returnsCorrectMAC() {
        assertEquals(DST_MAC, header(64).getDestinationMAC());
    }

    @Test
    void getTtl_returnsCorrectTTL() {
        assertEquals(64, header(64).getTtl());
    }

    // ─────────────────────────────────────────────────────────────────
    // isExpired()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void isExpired_freshHeader_returnsFalse() {
        assertFalse(header(64).isExpired());
    }

    @Test
    void isExpired_ttlOne_returnsFalse() {
        assertFalse(header(1).isExpired());
    }

    @Test
    void isExpired_afterDecrementFromOne_returnsTrue() {
        PacketHeader expired = header(1).decrementTTL();
        assertTrue(expired.isExpired());
    }

    // ─────────────────────────────────────────────────────────────────
    // decrementTTL()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void decrementTTL_ttlAboveOne_returnsSmallerTTL() {
        PacketHeader decremented = header(64).decrementTTL();
        assertEquals(63, decremented.getTtl());
    }

    @Test
    void decrementTTL_returnsNewInstance() {
        PacketHeader original = header(64);
        PacketHeader decremented = original.decrementTTL();
        assertNotSame(original, decremented);
    }

    @Test
    void decrementTTL_originalUnchanged() {
        PacketHeader original = header(64);
        original.decrementTTL();
        assertEquals(64, original.getTtl());
    }

    @Test
    void decrementTTL_ttlOne_returnsExpiredHeader() {
        PacketHeader expired = header(1).decrementTTL();
        assertEquals(0, expired.getTtl());
        assertTrue(expired.isExpired());
    }

    @Test
    void decrementTTL_onExpiredHeader_throwsIllegalStateException() {
        PacketHeader expired = header(1).decrementTTL();
        assertThrows(IllegalStateException.class, expired::decrementTTL);
    }

    @Test
    void decrementTTL_chainedTwice_ttlReducedByTwo() {
        PacketHeader h = header(10).decrementTTL().decrementTTL();
        assertEquals(8, h.getTtl());
    }

    // ─────────────────────────────────────────────────────────────────
    // equals() and hashCode()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void equals_identicalHeaders_returnsTrue() {
        PacketHeader a = header(64);
        PacketHeader b = new PacketHeader(srcIP, dstIP, SRC_MAC, DST_MAC, 64);
        assertEquals(a, b);
    }

    @Test
    void equals_differentTTL_returnsFalse() {
        assertNotEquals(header(64), header(128));
    }

    @Test
    void equals_differentSourceIP_returnsFalse() {
        PacketHeader a = header(64);
        PacketHeader b = new PacketHeader(
            new IPAddress("10.0.0.1"), dstIP, SRC_MAC, DST_MAC, 64);
        assertNotEquals(a, b);
    }

    @Test
    void equals_differentDestinationIP_returnsFalse() {
        PacketHeader a = header(64);
        PacketHeader b = new PacketHeader(
            srcIP, new IPAddress("10.0.0.2"), SRC_MAC, DST_MAC, 64);
        assertNotEquals(a, b);
    }

    @Test
    void equals_differentSourceMAC_returnsFalse() {
        PacketHeader a = header(64);
        PacketHeader b = new PacketHeader(
            srcIP, dstIP, "FF:EE:DD:CC:BB:AA", DST_MAC, 64);
        assertNotEquals(a, b);
    }

    @Test
    void equals_differentDestinationMAC_returnsFalse() {
        PacketHeader a = header(64);
        PacketHeader b = new PacketHeader(
            srcIP, dstIP, SRC_MAC, "FF:EE:DD:CC:BB:AA", 64);
        assertNotEquals(a, b);
    }

    @Test
    void equals_sameInstance_returnsTrue() {
        PacketHeader h = header(64);
        assertEquals(h, h);
    }

    @Test
    void equals_null_returnsFalse() {
        assertNotEquals(null, header(64));
    }

    @Test
    void equals_differentType_returnsFalse() {
        assertNotEquals("not a header", header(64));
    }

    @Test
    void hashCode_equalHeaders_samehashCode() {
        PacketHeader a = header(64);
        PacketHeader b = new PacketHeader(srcIP, dstIP, SRC_MAC, DST_MAC, 64);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void hashCode_differentHeaders_differentHashCode() {
        assertNotEquals(header(64).hashCode(), header(128).hashCode());
    }

    // ─────────────────────────────────────────────────────────────────
    // toString()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void toString_containsSourceIP() {
        assertTrue(header(64).toString().contains("192.168.1.1"));
    }

    @Test
    void toString_containsDestinationIP() {
        assertTrue(header(64).toString().contains("192.168.1.2"));
    }

    @Test
    void toString_containsTTL() {
        assertTrue(header(64).toString().contains("64"));
    }
}