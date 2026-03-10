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
 * Tests for DHCPServer.
 *
 * Input partitions covered:
 *  - Construction: correct DeviceType, leases empty, pool null
 *  - setPool(): valid start+end stored, null start throws, null end throws
 *  - getPoolStart/getPoolEnd(): null before configuration, correct after
 *  - assignIP(): pool not configured returns empty, first assignment returns
 *    first IP in pool, second assignment returns second IP,
 *    same MAC re-assigned gets a new IP (or whichever is free),
 *    null mac throws, blank mac throws, pool exhausted returns empty
 *  - releaseIP(): known MAC released returns true and IP freed,
 *    unknown MAC returns false
 *  - getLeases(): unmodifiable
 *  - receivePacket(): valid packet accepted, null throws
 *  - reset(): clears leases and pool
 */
class TestDHCPServer {

    private static final String SRC_MAC = "AA:BB:CC:DD:EE:FF";
    private static final String DST_MAC = "11:22:33:44:55:66";

    private DHCPServer dhcp;

    @BeforeEach
    void setUp() {
        dhcp = new DHCPServer("DHCP-1");
    }

    private Packet samplePacket() {
        PacketHeader h = new PacketHeader(
            new IPAddress("0.0.0.0"), new IPAddress("255.255.255.255"),
            SRC_MAC, DST_MAC, 64);
        return new Packet(PacketType.DHCP_DISCOVER, h, "");
    }

    // ─────────────────────────────────────────────────────────────────
    // Construction
    // ─────────────────────────────────────────────────────────────────

    @Test
    void constructor_setsCorrectDeviceType() {
        assertEquals(DeviceType.DHCP_SERVER, dhcp.getDeviceType());
    }

    @Test
    void constructor_leasesEmptyInitially() {
        assertTrue(dhcp.getLeases().isEmpty());
    }

    @Test
    void constructor_poolStartIsNull() {
        assertNull(dhcp.getPoolStart());
    }

    @Test
    void constructor_poolEndIsNull() {
        assertNull(dhcp.getPoolEnd());
    }

    // ─────────────────────────────────────────────────────────────────
    // setPool()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void setPool_validArgs_storesStartAndEnd() {
        IPAddress start = new IPAddress("192.168.1.100");
        IPAddress end   = new IPAddress("192.168.1.200");
        dhcp.setPool(start, end);
        assertEquals(start, dhcp.getPoolStart());
        assertEquals(end,   dhcp.getPoolEnd());
    }

    @Test
    void setPool_nullStart_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> dhcp.setPool(null, new IPAddress("192.168.1.200")));
    }

    @Test
    void setPool_nullEnd_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> dhcp.setPool(new IPAddress("192.168.1.100"), null));
    }

    // ─────────────────────────────────────────────────────────────────
    // assignIP()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void assignIP_poolNotConfigured_returnsEmpty() {
        assertTrue(dhcp.assignIP("AA:BB:CC:DD:EE:FF").isEmpty());
    }

    @Test
    void assignIP_firstAssignment_returnsFirstIPInPool() {
        dhcp.setPool(new IPAddress("10.0.0.1"), new IPAddress("10.0.0.10"));
        Optional<IPAddress> assigned = dhcp.assignIP("AA:BB:CC:DD:EE:FF");
        assertTrue(assigned.isPresent());
        assertEquals(new IPAddress("10.0.0.1"), assigned.get());
    }

    @Test
    void assignIP_secondAssignment_returnsDifferentIP() {
        dhcp.setPool(new IPAddress("10.0.0.1"), new IPAddress("10.0.0.10"));
        dhcp.assignIP("AA:BB:CC:DD:EE:FF");
        Optional<IPAddress> second = dhcp.assignIP("11:22:33:44:55:66");
        assertTrue(second.isPresent());
        assertEquals(new IPAddress("10.0.0.2"), second.get());
    }

    @Test
    void assignIP_leasedToMac_appearsInLeaseTable() {
        dhcp.setPool(new IPAddress("10.0.0.1"), new IPAddress("10.0.0.10"));
        dhcp.assignIP("AA:BB:CC:DD:EE:FF");
        assertTrue(dhcp.getLeases().containsKey("AA:BB:CC:DD:EE:FF"));
    }

    @Test
    void assignIP_poolExhausted_returnsEmpty() {
        // Pool of exactly one IP
        dhcp.setPool(new IPAddress("10.0.0.1"), new IPAddress("10.0.0.1"));
        dhcp.assignIP("AA:BB:CC:DD:EE:FF");
        assertTrue(dhcp.assignIP("11:22:33:44:55:66").isEmpty());
    }

    @Test
    void assignIP_nullMac_throwsIllegalArgumentException() {
        dhcp.setPool(new IPAddress("10.0.0.1"), new IPAddress("10.0.0.10"));
        assertThrows(IllegalArgumentException.class,
            () -> dhcp.assignIP(null));
    }

    @Test
    void assignIP_blankMac_throwsIllegalArgumentException() {
        dhcp.setPool(new IPAddress("10.0.0.1"), new IPAddress("10.0.0.10"));
        assertThrows(IllegalArgumentException.class,
            () -> dhcp.assignIP("  "));
    }

    // ─────────────────────────────────────────────────────────────────
    // releaseIP()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void releaseIP_knownMAC_returnsTrue() {
        dhcp.setPool(new IPAddress("10.0.0.1"), new IPAddress("10.0.0.10"));
        dhcp.assignIP("AA:BB:CC:DD:EE:FF");
        assertTrue(dhcp.releaseIP("AA:BB:CC:DD:EE:FF"));
    }

    @Test
    void releaseIP_knownMAC_removesFromLeaseTable() {
        dhcp.setPool(new IPAddress("10.0.0.1"), new IPAddress("10.0.0.10"));
        dhcp.assignIP("AA:BB:CC:DD:EE:FF");
        dhcp.releaseIP("AA:BB:CC:DD:EE:FF");
        assertFalse(dhcp.getLeases().containsKey("AA:BB:CC:DD:EE:FF"));
    }

    @Test
    void releaseIP_knownMAC_freesMacForReassignment() {
        dhcp.setPool(new IPAddress("10.0.0.1"), new IPAddress("10.0.0.1"));
        dhcp.assignIP("AA:BB:CC:DD:EE:FF");
        dhcp.releaseIP("AA:BB:CC:DD:EE:FF");
        assertTrue(dhcp.assignIP("11:22:33:44:55:66").isPresent());
    }

    @Test
    void releaseIP_unknownMAC_returnsFalse() {
        assertFalse(dhcp.releaseIP("00:00:00:00:00:00"));
    }

    // ─────────────────────────────────────────────────────────────────
    // getLeases()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void getLeases_returnsUnmodifiableMap() {
        assertThrows(UnsupportedOperationException.class,
            () -> dhcp.getLeases().put("AA:BB:CC:DD:EE:FF",
                new IPAddress("10.0.0.1")));
    }

    // ─────────────────────────────────────────────────────────────────
    // receivePacket()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void receivePacket_validPacket_doesNotThrow() {
        assertDoesNotThrow(() -> dhcp.receivePacket(samplePacket()));
    }

    @Test
    void receivePacket_null_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> dhcp.receivePacket(null));
    }

    // ─────────────────────────────────────────────────────────────────
    // reset()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void reset_clearsLeases() {
        dhcp.setPool(new IPAddress("10.0.0.1"), new IPAddress("10.0.0.10"));
        dhcp.assignIP("AA:BB:CC:DD:EE:FF");
        dhcp.reset();
        assertTrue(dhcp.getLeases().isEmpty());
    }

    @Test
    void reset_clearsPool() {
        dhcp.setPool(new IPAddress("10.0.0.1"), new IPAddress("10.0.0.10"));
        dhcp.reset();
        assertNull(dhcp.getPoolStart());
        assertNull(dhcp.getPoolEnd());
    }
}