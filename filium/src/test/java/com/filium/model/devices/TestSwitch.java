package com.filium.model.devices;

import com.filium.model.network.IPAddress;
import com.filium.model.network.NetworkInterface;
import com.filium.packet.Packet;
import com.filium.packet.PacketHeader;
import com.filium.packet.PacketType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Switch.
 *
 * Input partitions covered:
 *  - Construction: correct DeviceType, one port present, MAC table empty
 *  - addPort(): valid port added, null throws, at MAX_PORTS throws
 *  - getPorts(): unmodifiable, grows as ports added
 *  - learnMAC(): valid mac+port, null mac throws, blank mac throws, null port throws
 *  - lookupMAC(): found, not found
 *  - getMacTable(): unmodifiable
 *  - receivePacket(): valid packet accepted, null throws
 *  - reset(): clears MAC table and extra ports, retains base interface
 */
class TestSwitch {

    private static final String SRC_MAC = "AA:BB:CC:DD:EE:FF";
    private static final String DST_MAC = "11:22:33:44:55:66";

    private Switch sw;

    @BeforeEach
    void setUp() {
        sw = new Switch("Switch-1");
    }

    private Packet samplePacket() {
        PacketHeader h = new PacketHeader(
            new IPAddress("192.168.1.1"), new IPAddress("192.168.1.2"),
            SRC_MAC, DST_MAC, 64);
        return new Packet(PacketType.ARP_REQUEST, h, "");
    }

    private NetworkInterface newPort(int i) {
        return new NetworkInterface(
            String.format("%02X:%02X:%02X:%02X:%02X:%02X", i, i, i, i, i, i));
    }

    // ─────────────────────────────────────────────────────────────────
    // Construction
    // ─────────────────────────────────────────────────────────────────

    @Test
    void constructor_setsCorrectDeviceType() {
        assertEquals(DeviceType.SWITCH, sw.getDeviceType());
    }

    @Test
    void constructor_hasOnePortByDefault() {
        assertEquals(1, sw.getPorts().size());
    }

    @Test
    void constructor_macTableIsEmpty() {
        assertTrue(sw.getMacTable().isEmpty());
    }

    // ─────────────────────────────────────────────────────────────────
    // addPort()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void addPort_validPort_increasesCount() {
        sw.addPort(newPort(2));
        assertEquals(2, sw.getPorts().size());
    }

    @Test
    void addPort_null_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> sw.addPort(null));
    }

    @Test
    void addPort_atMaxPorts_throwsIllegalStateException() {
        for (int i = 1; i < Switch.MAX_PORTS; i++) {
            sw.addPort(newPort(i));
        }
        assertEquals(Switch.MAX_PORTS, sw.getPorts().size());
        assertThrows(IllegalStateException.class,
            () -> sw.addPort(new NetworkInterface("FF:FF:FF:FF:FF:FF")));
    }

    // ─────────────────────────────────────────────────────────────────
    // getPorts()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void getPorts_returnsUnmodifiableList() {
        assertThrows(UnsupportedOperationException.class,
            () -> sw.getPorts().clear());
    }

    // ─────────────────────────────────────────────────────────────────
    // learnMAC()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void learnMAC_validArgs_storesMapping() {
        NetworkInterface port = newPort(2);
        sw.learnMAC("AA:BB:CC:DD:EE:FF", port);
        assertEquals(port, sw.lookupMAC("AA:BB:CC:DD:EE:FF"));
    }

    @Test
    void learnMAC_nullMAC_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> sw.learnMAC(null, newPort(2)));
    }

    @Test
    void learnMAC_blankMAC_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> sw.learnMAC("   ", newPort(2)));
    }

    @Test
    void learnMAC_nullPort_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> sw.learnMAC("AA:BB:CC:DD:EE:FF", null));
    }

    // ─────────────────────────────────────────────────────────────────
    // lookupMAC()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void lookupMAC_knownMAC_returnsPort() {
        NetworkInterface port = newPort(3);
        sw.learnMAC("AA:BB:CC:DD:EE:FF", port);
        assertNotNull(sw.lookupMAC("AA:BB:CC:DD:EE:FF"));
    }

    @Test
    void lookupMAC_unknownMAC_returnsNull() {
        assertNull(sw.lookupMAC("00:00:00:00:00:00"));
    }

    // ─────────────────────────────────────────────────────────────────
    // getMacTable()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void getMacTable_returnsUnmodifiableMap() {
        assertThrows(UnsupportedOperationException.class,
            () -> sw.getMacTable().put("AA:BB:CC:DD:EE:FF", newPort(2)));
    }

    // ─────────────────────────────────────────────────────────────────
    // receivePacket()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void receivePacket_validPacket_doesNotThrow() {
        assertDoesNotThrow(() -> sw.receivePacket(samplePacket()));
    }

    @Test
    void receivePacket_null_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> sw.receivePacket(null));
    }

    // ─────────────────────────────────────────────────────────────────
    // reset()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void reset_clearsMacTable() {
        sw.learnMAC("AA:BB:CC:DD:EE:FF", newPort(2));
        sw.reset();
        assertTrue(sw.getMacTable().isEmpty());
    }

    @Test
    void reset_restoresToSinglePort() {
        sw.addPort(newPort(2));
        sw.addPort(newPort(3));
        sw.reset();
        assertEquals(1, sw.getPorts().size());
    }

    @Test
    void reset_basePortIsRetained() {
        NetworkInterface base = sw.getPorts().get(0);
        sw.addPort(newPort(2));
        sw.reset();
        assertEquals(base, sw.getPorts().get(0));
    }
}