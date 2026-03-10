package com.filium.simulation.protocols;

import com.filium.model.devices.DHCPServer;
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

class TestDHCPHandler {

    private static final String MAC_CLIENT = "CC:CC:CC:CC:CC:CC";
    private static final String MAC_DHCP   = "DD:DD:DD:DD:DD:DD";

    private DHCPHandler handler;
    private NetworkTopology topology;
    private PacketQueue queue;
    private PC client;
    private DHCPServer dhcpServer;

    @BeforeEach void setUp() {
        handler    = new DHCPHandler();
        topology   = new NetworkTopology();
        queue      = new PacketQueue();
        client     = new PC("Client");
        client.getNetworkInterface().setIpAddress(new IPAddress("0.0.0.0"));
        dhcpServer = new DHCPServer("DHCP-1");
        dhcpServer.getNetworkInterface().setIpAddress(new IPAddress("10.0.0.1"));
        dhcpServer.setPool(new IPAddress("10.0.0.100"), new IPAddress("10.0.0.110"));
        topology.addDevice(client);
        topology.addDevice(dhcpServer);
    }

    private Packet discover() {
        PacketHeader h = new PacketHeader(
            new IPAddress("0.0.0.0"), new IPAddress("255.255.255.255"),
            MAC_CLIENT, MAC_DHCP, 64);
        return new Packet(PacketType.DHCP_DISCOVER, h, "");
    }

    private Packet request() {
        PacketHeader h = new PacketHeader(
            new IPAddress("0.0.0.0"), new IPAddress("255.255.255.255"),
            MAC_CLIENT, MAC_DHCP, 64);
        return new Packet(PacketType.DHCP_REQUEST, h, "");
    }

    private Packet offer() {
        PacketHeader h = new PacketHeader(
            new IPAddress("10.0.0.1"), new IPAddress("0.0.0.0"),
            MAC_DHCP, MAC_CLIENT, 64);
        return new Packet(PacketType.DHCP_OFFER, h, "10.0.0.100");
    }

    private Packet ack() {
        PacketHeader h = new PacketHeader(
            new IPAddress("10.0.0.1"), new IPAddress("0.0.0.0"),
            MAC_DHCP, MAC_CLIENT, 64);
        return new Packet(PacketType.DHCP_ACK, h, "10.0.0.100");
    }

    // canHandle
    @Test void canHandle_discover_returnsTrue() { assertTrue(handler.canHandle(discover())); }
    @Test void canHandle_request_returnsTrue()  { assertTrue(handler.canHandle(request())); }
    @Test void canHandle_offer_returnsTrue()    { assertTrue(handler.canHandle(offer())); }
    @Test void canHandle_ack_returnsTrue()      { assertTrue(handler.canHandle(ack())); }
    @Test void canHandle_icmp_returnsFalse() {
        PacketHeader h = new PacketHeader(
            new IPAddress("10.0.0.1"), new IPAddress("10.0.0.2"),
            MAC_CLIENT, MAC_DHCP, 64);
        assertFalse(handler.canHandle(new Packet(PacketType.ICMP_ECHO_REQUEST, h, "")));
    }

    // handle — offer/ack passthrough
    @Test void handle_offer_emitsReceivedEvent() {
        List<SimulationEvent> e = handler.handle(offer(), topology, queue);
        assertEquals(SimulationEventType.PACKET_RECEIVED, e.get(0).getType());
    }
    @Test void handle_ack_emitsReceivedEvent() {
        List<SimulationEvent> e = handler.handle(ack(), topology, queue);
        assertEquals(SimulationEventType.PACKET_RECEIVED, e.get(0).getType());
    }

    // handle — discover
    @Test void handle_discover_enqueuesOffer() {
        handler.handle(discover(), topology, queue);
        assertEquals(PacketType.DHCP_OFFER, queue.dequeue().orElseThrow().getType());
    }
    @Test void handle_discover_emitsDHCPAssignedEvent() {
        List<SimulationEvent> e = handler.handle(discover(), topology, queue);
        assertEquals(SimulationEventType.DHCP_ASSIGNED, e.get(0).getType());
    }

    // handle — request
    @Test void handle_request_enqueuesAck() {
        handler.handle(request(), topology, queue);
        assertEquals(PacketType.DHCP_ACK, queue.dequeue().orElseThrow().getType());
    }

    // handle — no DHCP server
    @Test void handle_noDHCPServer_emitsDroppedEvent() {
        NetworkTopology t = new NetworkTopology(); t.addDevice(client);
        List<SimulationEvent> e = handler.handle(discover(), t, queue);
        assertEquals(SimulationEventType.PACKET_DROPPED, e.get(0).getType());
    }

    // handle — pool exhausted
    @Test void handle_poolExhausted_emitsDroppedEvent() {
        dhcpServer.setPool(new IPAddress("10.0.0.1"), new IPAddress("10.0.0.1"));
        dhcpServer.assignIP(MAC_CLIENT); // exhaust pool
        PacketHeader h = new PacketHeader(
            new IPAddress("0.0.0.0"), new IPAddress("255.255.255.255"),
            "EE:EE:EE:EE:EE:EE", MAC_DHCP, 64);
        Packet p = new Packet(PacketType.DHCP_DISCOVER, h, "");
        List<SimulationEvent> e = handler.handle(p, topology, queue);
        assertEquals(SimulationEventType.PACKET_DROPPED, e.get(0).getType());
    }

    // handle — DHCP server without IP
    @Test void handle_dhcpServerNoIP_emitsDroppedEvent() {
        DHCPServer noIP = new DHCPServer("DHCP-NoIP");
        NetworkTopology t = new NetworkTopology();
        t.addDevice(client); t.addDevice(noIP);
        List<SimulationEvent> e = handler.handle(discover(), t, queue);
        assertEquals(SimulationEventType.PACKET_DROPPED, e.get(0).getType());
    }
}