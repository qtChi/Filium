package com.filium.simulation.protocols;

import com.filium.model.network.IPAddress;
import com.filium.model.network.NetworkTopology;
import com.filium.packet.Packet;
import com.filium.packet.PacketHeader;
import com.filium.packet.PacketType;
import com.filium.simulation.PacketQueue;
import com.filium.simulation.SimulationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestProtocolRegistry {

    private ProtocolRegistry registry;
    private static final String MAC_A = "AA:BB:CC:DD:EE:FF";
    private static final String MAC_B = "11:22:33:44:55:66";

    // Minimal stub protocol for testing
    private Protocol icmpStub() {
        return new Protocol() {
            @Override public boolean canHandle(Packet p) {
                return p.getType() == PacketType.ICMP_ECHO_REQUEST;
            }
            @Override public List<SimulationEvent> handle(
                    Packet p, NetworkTopology t, PacketQueue q) {
                return List.of();
            }
        };
    }

    private Packet icmpPacket() {
        PacketHeader h = new PacketHeader(
            new IPAddress("10.0.0.1"), new IPAddress("10.0.0.2"), MAC_A, MAC_B, 64);
        return new Packet(PacketType.ICMP_ECHO_REQUEST, h, "");
    }

    private Packet dnsPacket() {
        PacketHeader h = new PacketHeader(
            new IPAddress("10.0.0.1"), new IPAddress("10.0.0.2"), MAC_A, MAC_B, 64);
        return new Packet(PacketType.DNS_QUERY, h, "host");
    }

    @BeforeEach void setUp() { registry = new ProtocolRegistry(); }

    @Test void constructor_isEmpty() { assertEquals(0, registry.size()); }

    @Test void register_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> registry.register(null));
    }
    @Test void register_validProtocol_increasesSize() {
        registry.register(icmpStub()); assertEquals(1, registry.size());
    }

    @Test void unregister_registeredProtocol_returnsTrue() {
        Protocol p = icmpStub(); registry.register(p);
        assertTrue(registry.unregister(p));
    }
    @Test void unregister_registeredProtocol_decreasesSize() {
        Protocol p = icmpStub(); registry.register(p);
        registry.unregister(p); assertEquals(0, registry.size());
    }
    @Test void unregister_unregisteredProtocol_returnsFalse() {
        assertFalse(registry.unregister(icmpStub()));
    }

    @Test void findHandler_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> registry.findHandler(null));
    }
    @Test void findHandler_matchingProtocol_returnsIt() {
        Protocol p = icmpStub(); registry.register(p);
        assertTrue(registry.findHandler(icmpPacket()).isPresent());
    }
    @Test void findHandler_noMatch_returnsEmpty() {
        registry.register(icmpStub());
        assertTrue(registry.findHandler(dnsPacket()).isEmpty());
    }
    @Test void findHandler_firstMatchReturned() {
        Protocol first = icmpStub(); Protocol second = icmpStub();
        registry.register(first); registry.register(second);
        assertEquals(first, registry.findHandler(icmpPacket()).get());
    }

    @Test void getHandlers_returnsUnmodifiableList() {
        assertThrows(UnsupportedOperationException.class,
            () -> registry.getHandlers().add(icmpStub()));
    }
}