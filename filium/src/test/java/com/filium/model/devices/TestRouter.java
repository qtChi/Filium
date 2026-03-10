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
 * Tests for Router.
 *
 * Input partitions covered:
 *  - Construction: correct DeviceType, one interface present by default
 *  - addInterface(): valid interface added, null throws,
 *    adding to MAX_INTERFACES throws
 *  - getInterfaces(): unmodifiable, grows as interfaces added
 *  - receivePacket(): valid packet accepted, null throws
 *  - reset(): restores to single base interface
 */
class TestRouter {

    private static final String SRC_MAC = "AA:BB:CC:DD:EE:FF";
    private static final String DST_MAC = "11:22:33:44:55:66";

    private Router router;

    @BeforeEach
    void setUp() {
        router = new Router("Router-1");
    }

    private Packet samplePacket() {
        PacketHeader h = new PacketHeader(
            new IPAddress("192.168.1.1"), new IPAddress("10.0.0.1"),
            SRC_MAC, DST_MAC, 64);
        return new Packet(PacketType.ICMP_ECHO_REQUEST, h, "");
    }

    // ─────────────────────────────────────────────────────────────────
    // Construction
    // ─────────────────────────────────────────────────────────────────

    @Test
    void constructor_setsCorrectDeviceType() {
        assertEquals(DeviceType.ROUTER, router.getDeviceType());
    }

    @Test
    void constructor_hasOneInterfaceByDefault() {
        assertEquals(1, router.getInterfaces().size());
    }

    // ─────────────────────────────────────────────────────────────────
    // addInterface()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void addInterface_validInterface_increasesCount() {
        router.addInterface(new NetworkInterface("BB:BB:BB:BB:BB:BB"));
        assertEquals(2, router.getInterfaces().size());
    }

    @Test
    void addInterface_null_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> router.addInterface(null));
    }

    @Test
    void addInterface_atMaxInterfaces_throwsIllegalStateException() {
        // Already has 1; add 7 more to reach MAX_INTERFACES (8)
        for (int i = 1; i < Router.MAX_INTERFACES; i++) {
            router.addInterface(new NetworkInterface(
                String.format("%02X:%02X:%02X:%02X:%02X:%02X", i, i, i, i, i, i)));
        }
        assertEquals(Router.MAX_INTERFACES, router.getInterfaces().size());
        assertThrows(IllegalStateException.class,
            () -> router.addInterface(new NetworkInterface("FF:FF:FF:FF:FF:FF")));
    }

    // ─────────────────────────────────────────────────────────────────
    // getInterfaces()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void getInterfaces_returnsUnmodifiableList() {
        assertThrows(UnsupportedOperationException.class,
            () -> router.getInterfaces().clear());
    }

    // ─────────────────────────────────────────────────────────────────
    // receivePacket()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void receivePacket_validPacket_doesNotThrow() {
        assertDoesNotThrow(() -> router.receivePacket(samplePacket()));
    }

    @Test
    void receivePacket_null_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> router.receivePacket(null));
    }

    // ─────────────────────────────────────────────────────────────────
    // reset()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void reset_restoresToSingleInterface() {
        router.addInterface(new NetworkInterface("BB:BB:BB:BB:BB:BB"));
        router.addInterface(new NetworkInterface("CC:CC:CC:CC:CC:CC"));
        router.reset();
        assertEquals(1, router.getInterfaces().size());
    }

    @Test
    void reset_baseInterfaceIsRetained() {
        NetworkInterface base = router.getInterfaces().get(0);
        router.addInterface(new NetworkInterface("BB:BB:BB:BB:BB:BB"));
        router.reset();
        assertEquals(base, router.getInterfaces().get(0));
    }
}