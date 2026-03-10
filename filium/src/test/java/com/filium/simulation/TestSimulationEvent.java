package com.filium.simulation;

import com.filium.model.devices.PC;
import com.filium.model.network.IPAddress;
import com.filium.packet.Packet;
import com.filium.packet.PacketHeader;
import com.filium.packet.PacketType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestSimulationEvent {

    private PC src, dst;
    private Packet packet;
    private static final String MAC_A = "AA:BB:CC:DD:EE:FF";
    private static final String MAC_B = "11:22:33:44:55:66";

    @BeforeEach void setUp() {
        src = new PC("PC-A"); dst = new PC("PC-B");
        PacketHeader h = new PacketHeader(
            new IPAddress("10.0.0.1"), new IPAddress("10.0.0.2"), MAC_A, MAC_B, 64);
        packet = new Packet(PacketType.ICMP_ECHO_REQUEST, h, "");
    }

    private SimulationEvent event() {
        return new SimulationEvent(SimulationEventType.PACKET_SENT,
            src, dst, packet, "test event");
    }

    // Construction validation
    @Test void constructor_nullType_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> new SimulationEvent(null, src, dst, packet, "msg"));
    }
    @Test void constructor_nullMessage_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> new SimulationEvent(SimulationEventType.PACKET_SENT,
                src, dst, packet, null));
    }
    @Test void constructor_nullSourceAndDestination_allowed() {
        assertDoesNotThrow(() -> new SimulationEvent(
            SimulationEventType.PACKET_SENT, null, null, null, "msg"));
    }

    // Getters
    @Test void getType_returnsCorrectType() {
        assertEquals(SimulationEventType.PACKET_SENT, event().getType());
    }
    @Test void getSource_returnsCorrectDevice() { assertEquals(src, event().getSource()); }
    @Test void getDestination_returnsCorrectDevice() { assertEquals(dst, event().getDestination()); }
    @Test void getPacket_returnsCorrectPacket() { assertEquals(packet, event().getPacket()); }
    @Test void getMessage_returnsCorrectMessage() {
        assertEquals("test event", event().getMessage());
    }
    @Test void getTimestamp_returnsPositive() { assertTrue(event().getTimestamp() > 0); }

    // toString
    @Test void toString_containsType() {
        assertTrue(event().toString().contains("PACKET_SENT"));
    }
    @Test void toString_nullSource_showsNull() {
        SimulationEvent e = new SimulationEvent(
            SimulationEventType.PACKET_SENT, null, dst, packet, "msg");
        assertTrue(e.toString().contains("null"));
    }

    // equals / hashCode
    @Test void equals_sameInstance_returnsTrue() {
        SimulationEvent e = event(); assertEquals(e, e);
    }
    @Test void equals_differentInstances_returnsFalse() {
        // timestamps will differ
        assertNotEquals(event(), event());
    }
    @Test void equals_null_returnsFalse() { assertNotEquals(null, event()); }
    @Test void equals_differentType_returnsFalse() { assertNotEquals("x", event()); }
    @Test void hashCode_sameInstance_consistent() {
        SimulationEvent e = event();
        assertEquals(e.hashCode(), e.hashCode());
    }
}