package com.filium.simulation.protocols;

import com.filium.model.devices.DNSServer;
import com.filium.model.devices.PC;
import com.filium.model.network.IPAddress;
import com.filium.model.network.NetworkTopology;
import com.filium.packet.Packet;
import com.filium.packet.PacketHeader;
import com.filium.packet.PacketType;
import com.filium.simulation.PacketQueue;
import com.filium.simulation.SimulationEvent;
import com.filium.simulation.SimulationEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestDNSHandler {

    private static final String MAC_A = "AA:BB:CC:DD:EE:FF";
    private static final String MAC_DNS = "DD:DD:DD:DD:DD:DD";

    private DNSHandler handler;
    private NetworkTopology topology;
    private PacketQueue queue;
    private PC client;
    private DNSServer dnsServer;

    @BeforeEach void setUp() {
        handler   = new DNSHandler();
        topology  = new NetworkTopology();
        queue     = new PacketQueue();
        client    = new PC("Client");
        client.getNetworkInterface().setIpAddress(new IPAddress("10.0.0.1"));
        dnsServer = new DNSServer("DNS-1");
        dnsServer.getNetworkInterface().setIpAddress(new IPAddress("10.0.0.53"));
        dnsServer.addRecord("example.com", new IPAddress("93.184.216.34"));
        topology.addDevice(client);
        topology.addDevice(dnsServer);
    }

    private Packet query(String hostname) {
        PacketHeader h = new PacketHeader(
            new IPAddress("10.0.0.1"), new IPAddress("10.0.0.53"), MAC_A, MAC_DNS, 64);
        return new Packet(PacketType.DNS_QUERY, h, hostname);
    }

    private Packet response() {
        PacketHeader h = new PacketHeader(
            new IPAddress("10.0.0.53"), new IPAddress("10.0.0.1"), MAC_DNS, MAC_A, 64);
        return new Packet(PacketType.DNS_RESPONSE, h, "93.184.216.34");
    }

    // canHandle
    @Test void canHandle_dnsQuery_returnsTrue()    { assertTrue(handler.canHandle(query("x"))); }
    @Test void canHandle_dnsResponse_returnsTrue() { assertTrue(handler.canHandle(response())); }
    @Test void canHandle_arpRequest_returnsFalse() {
        PacketHeader h = new PacketHeader(
            new IPAddress("10.0.0.1"), new IPAddress("10.0.0.2"), MAC_A, MAC_DNS, 64);
        assertFalse(handler.canHandle(new Packet(PacketType.ARP_REQUEST, h, "")));
    }

    // handle — response passthrough
    @Test void handle_dnsResponse_emitsReceivedEvent() {
        List<SimulationEvent> events = handler.handle(response(), topology, queue);
        assertEquals(SimulationEventType.PACKET_RECEIVED, events.get(0).getType());
    }
    @Test void handle_dnsResponse_doesNotEnqueuePacket() {
        handler.handle(response(), topology, queue);
        assertTrue(queue.isEmpty());
    }

    // handle — query resolved
    @Test void handle_queryResolved_enqueuesResponse() {
        handler.handle(query("example.com"), topology, queue);
        assertFalse(queue.isEmpty());
        assertEquals(PacketType.DNS_RESPONSE, queue.dequeue().orElseThrow().getType());
    }
    @Test void handle_queryResolved_emitsDNSResolvedEvent() {
        List<SimulationEvent> events = handler.handle(query("example.com"), topology, queue);
        assertEquals(SimulationEventType.DNS_RESOLVED, events.get(0).getType());
    }

    // handle — query not found
    @Test void handle_queryNotFound_emitsDNSFailedEvent() {
        List<SimulationEvent> events = handler.handle(query("notfound.com"), topology, queue);
        assertEquals(SimulationEventType.DNS_FAILED, events.get(0).getType());
    }
    @Test void handle_queryNotFound_doesNotEnqueuePacket() {
        handler.handle(query("notfound.com"), topology, queue);
        assertTrue(queue.isEmpty());
    }

    // handle — no DNS server
    @Test void handle_noDNSServer_emitsDNSFailedEvent() {
        NetworkTopology emptyTopology = new NetworkTopology();
        emptyTopology.addDevice(client);
        List<SimulationEvent> events =
            handler.handle(query("example.com"), emptyTopology, queue);
        assertEquals(SimulationEventType.DNS_FAILED, events.get(0).getType());
    }

    // handle — DNS server without IP
    @Test void handle_dnsServerNoIP_emitsDNSFailedEvent() {
        DNSServer noIP = new DNSServer("DNS-NoIP");
        NetworkTopology t = new NetworkTopology();
        t.addDevice(client); t.addDevice(noIP);
        List<SimulationEvent> events = handler.handle(query("example.com"), t, queue);
        assertEquals(SimulationEventType.DNS_FAILED, events.get(0).getType());
    }
}