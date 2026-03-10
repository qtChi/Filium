package com.filium.simulation.protocols;

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

class TestICMPHandler {

    private static final String MAC_A = "AA:BB:CC:DD:EE:FF";
    private static final String MAC_B = "11:22:33:44:55:66";

    private ICMPHandler handler;
    private NetworkTopology topology;
    private PacketQueue queue;
    private PC pcA, pcB;

    @BeforeEach void setUp() {
        handler  = new ICMPHandler();
        topology = new NetworkTopology();
        queue    = new PacketQueue();
        pcA = new PC("PC-A"); pcA.getNetworkInterface().setIpAddress(new IPAddress("10.0.0.1"));
        pcB = new PC("PC-B"); pcB.getNetworkInterface().setIpAddress(new IPAddress("10.0.0.2"));
        topology.addDevice(pcA); topology.addDevice(pcB);
    }

    private Packet request() {
        PacketHeader h = new PacketHeader(
            new IPAddress("10.0.0.1"), new IPAddress("10.0.0.2"), MAC_A, MAC_B, 64);
        return new Packet(PacketType.ICMP_ECHO_REQUEST, h, "ping");
    }

    private Packet reply() {
        PacketHeader h = new PacketHeader(
            new IPAddress("10.0.0.2"), new IPAddress("10.0.0.1"), MAC_B, MAC_A, 64);
        return new Packet(PacketType.ICMP_ECHO_REPLY, h, "ping");
    }

    // canHandle
    @Test void canHandle_echoRequest_returnsTrue() {
        assertTrue(handler.canHandle(request()));
    }
    @Test void canHandle_echoReply_returnsTrue() {
        assertTrue(handler.canHandle(reply()));
    }
    @Test void canHandle_dnsQuery_returnsFalse() {
        PacketHeader h = new PacketHeader(
            new IPAddress("10.0.0.1"), new IPAddress("10.0.0.2"), MAC_A, MAC_B, 64);
        assertFalse(handler.canHandle(new Packet(PacketType.DNS_QUERY, h, "")));
    }

    // handle — request with known destination
    @Test void handle_requestToKnownDst_enqueuesReply() {
        handler.handle(request(), topology, queue);
        assertFalse(queue.isEmpty());
        assertEquals(PacketType.ICMP_ECHO_REPLY,
            queue.dequeue().orElseThrow().getType());
    }
    @Test void handle_requestToKnownDst_emitsReceivedEvent() {
        List<SimulationEvent> events = handler.handle(request(), topology, queue);
        assertEquals(SimulationEventType.PACKET_RECEIVED, events.get(0).getType());
    }

    // handle — request with unknown destination
    @Test void handle_requestToUnknownDst_doesNotEnqueueReply() {
        PacketHeader h = new PacketHeader(
            new IPAddress("10.0.0.1"), new IPAddress("9.9.9.9"), MAC_A, MAC_B, 64);
        Packet p = new Packet(PacketType.ICMP_ECHO_REQUEST, h, "");
        handler.handle(p, topology, queue);
        assertTrue(queue.isEmpty());
    }
    @Test void handle_requestToUnknownDst_emitsDroppedEvent() {
        PacketHeader h = new PacketHeader(
            new IPAddress("10.0.0.1"), new IPAddress("9.9.9.9"), MAC_A, MAC_B, 64);
        Packet p = new Packet(PacketType.ICMP_ECHO_REQUEST, h, "");
        List<SimulationEvent> events = handler.handle(p, topology, queue);
        assertEquals(SimulationEventType.PACKET_DROPPED, events.get(0).getType());
    }

    // handle — reply
    @Test void handle_reply_emitsReceivedEvent() {
        List<SimulationEvent> events = handler.handle(reply(), topology, queue);
        assertEquals(SimulationEventType.PACKET_RECEIVED, events.get(0).getType());
    }
    @Test void handle_reply_doesNotEnqueuePacket() {
        handler.handle(reply(), topology, queue);
        assertTrue(queue.isEmpty());
    }
}