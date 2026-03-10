package com.filium.model.devices;

import com.filium.model.network.IPAddress;
import com.filium.packet.Packet;
import com.filium.packet.PacketHeader;
import com.filium.packet.PacketType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PC.
 *
 * Input partitions covered:
 *  - Construction: valid name, correct DeviceType assigned
 *  - receivePacket(): valid packet stored, null packet throws
 *  - getReceivedPackets(): empty initially, grows on receive,
 *    returns unmodifiable list
 *  - reset(): clears received packets
 */
class TestPC {

    private static final String SRC_MAC = "AA:BB:CC:DD:EE:FF";
    private static final String DST_MAC = "11:22:33:44:55:66";

    private PC pc;

    @BeforeEach
    void setUp() {
        pc = new PC("PC-1");
    }

    private Packet samplePacket() {
        PacketHeader h = new PacketHeader(
            new IPAddress("192.168.1.1"), new IPAddress("192.168.1.2"),
            SRC_MAC, DST_MAC, 64);
        return new Packet(PacketType.ICMP_ECHO_REQUEST, h, "ping");
    }

    // ─────────────────────────────────────────────────────────────────
    // Construction
    // ─────────────────────────────────────────────────────────────────

    @Test
    void constructor_setsCorrectDeviceType() {
        assertEquals(DeviceType.PC, pc.getDeviceType());
    }

    @Test
    void constructor_receivedPacketsEmptyInitially() {
        assertTrue(pc.getReceivedPackets().isEmpty());
    }

    // ─────────────────────────────────────────────────────────────────
    // receivePacket()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void receivePacket_validPacket_storedInInbox() {
        Packet p = samplePacket();
        pc.receivePacket(p);
        assertTrue(pc.getReceivedPackets().contains(p));
    }

    @Test
    void receivePacket_multiplePackets_allStored() {
        pc.receivePacket(samplePacket());
        pc.receivePacket(samplePacket());
        assertEquals(2, pc.getReceivedPackets().size());
    }

    @Test
    void receivePacket_null_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> pc.receivePacket(null));
    }

    // ─────────────────────────────────────────────────────────────────
    // getReceivedPackets()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void getReceivedPackets_returnsUnmodifiableList() {
        pc.receivePacket(samplePacket());
        assertThrows(UnsupportedOperationException.class,
            () -> pc.getReceivedPackets().clear());
    }

    // ─────────────────────────────────────────────────────────────────
    // reset()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void reset_clearsReceivedPackets() {
        pc.receivePacket(samplePacket());
        pc.reset();
        assertTrue(pc.getReceivedPackets().isEmpty());
    }

    @Test
    void reset_onEmptyInbox_doesNotThrow() {
        assertDoesNotThrow(() -> pc.reset());
    }
}